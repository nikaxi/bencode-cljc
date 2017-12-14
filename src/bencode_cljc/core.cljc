(ns bencode-cljc.core
  "A functional Clojure(script) BEncode serialization library."
  (:require [clojure.string :as string]))

(defn- parse-int [s]
  (when s
    #?(:clj (BigInteger. s)
       :cljs (js/parseInt s))))

(declare serialize)

(defn- map-reducer-fn [s [key val]]
  (when s
    (when-let [sk (serialize key)]
      (when-let [sv (serialize val)]
        (str s sk sv)))))

(defn- serialize-map [m]
  (when (every? (some-fn string? keyword?) (keys m))
    (when-let [serial (reduce map-reducer-fn "" (into (sorted-map) m))]
      (str "d" serial "e"))))

(defn- serialize-list [coll]
  (let [serialized-list (map serialize coll)]
    (when (not-any? nil? serialized-list)
      (str "l" (string/join (map serialize coll)) "e"))))

(declare deserialize-next)

;; Deserialization worker functions return a tuple. The first value contains
;; the parsed value, the second contains the rest of the unparsed string. The
;; unparsed string is formed by dropping the parsed section from the original
;; string. This mimics stream processing while keeping each function pure.
;;
;; A BEncoded string is considered finished when the unparsed end of the tuple
;; is empty, representing a fully parsed BEncoded string.
;;
;; As an implementation detail, this dangling stream is not exposed in the public
;; interface. However it is only necessary while implementing the containers map
;; and list. Any dangling stream that escapes the parser is caught in `deserialize`
;; and returns a `nil` value to indicate malformed strings.

(defn- deserialize-int [s]
  (when-let [[found stripped] (re-find #"i((?:-?[1-9]{1}[0-9]*)|(?:0))e" s)]
    (vector
      (parse-int stripped)
      (string/join (drop (count found) s)))))

(defn- deserialize-string [s]
  (when-let [[_ size-str string-shard] (re-find #"(\d+):(.*)" s)]
    (when-let [size (parse-int size-str)]
      (when-not (< (count string-shard) size)
        (vector
          (subs string-shard 0 size)
          (string/join (drop (+ 1 size (count size-str)) s)))))))

(defn- deserialize-list [s]
  (loop [leftover (string/join (next s))
         peek-ahead (first leftover)
         coll (vector)]
    (if (= \e peek-ahead)
      (vector (sequence coll) (string/join (next leftover)))
      (when-let [[found rest] (deserialize-next leftover)]
        (recur rest (first rest) (conj coll found))))))

(defn- deserialize-map [s]
  (when-let [[map-list rest] (deserialize-list s)]
    (when (even? (count map-list))
      (vector (apply hash-map map-list) rest))))

(defn- deserialize-next
  [encoded]
  (case (first (seq encoded))
    (\0 \1 \2 \3 \4 \5 \6 \7 \8 \9) (deserialize-string encoded)
    \i (deserialize-int encoded)
    \l (deserialize-list encoded)
    \d (deserialize-map encoded)
    nil))

(defn ^{:export true}
  serialize
  "Serialize a given Clojure(script) object to a BEncode string.

  Supports integers, strings, maps, and lists natively. Map keys must be strings, or coercable to strings.
  Coerces keywords to strings using 'name. Caution, this truncates namespaced keywords.
  Coerces vectors to list. Sets are not supported.
  Nil values are not supported. Consider an empty list.

  Returns the BEncoded string on success, and nil on failure"
  [object]
  (condp #(%1 %2) object
    integer? (str "i" object "e")
    string? (str (count object) ":" object)
    keyword? (serialize (name object))
    sequential? (serialize-list object)
    map? (serialize-map object)
    nil))

(defn ^{:export true}
  deserialize
  "Deserialize a BEncoded string into a Clojure(script) data structure.

  Returns a data structure on success, and nil on failure"
  [bencoded-str]
  (when (string? bencoded-str)
    (let [[parsed rest] (deserialize-next bencoded-str)]
      (when (string/blank? rest)
        parsed))))

(comment

  (let
    [f (partial re-find #"i((?:-?[1-9]{1}[0-9]*)|(?:0))e")
     ;f deserialize
     ]
    (vector
      (f "i0e")
      (f "i-0e")
      (f "i4e")
      (f "i-4e")
      (f "i09e")
      (f "i-09e")
      (f "i12345e")
      (f "i012345e")
      (f "i0012345e")
      (f "i-12345e")
      (f "i-012345e")
      (f "i-0012345e")
      (f "i123-45e")
      (f "i-123-45e")
      (f "i0012345e")))

  (let
    [f (partial re-find #"(\d+):(.*)")
     ;f deserialize
     ]
    (vector
      (f "0:")
      (f "-5:hello")
      (f "5:hello")
      (f "7:goodbye")
      (f "11:hello world")
      (f "11:hello worldi10e")
      (f "7:goodbyei10e")
      (f "7:goodbye4:next")
      ))
  )
