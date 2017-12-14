(ns bencode-cljc.core-test
  (:require
    #?(:clj  [clojure.test :refer [is testing deftest run-tests]]
       :cljs [cljs.test :refer [is testing deftest run-tests]])
    [bencode-cljc.core :refer [serialize deserialize]]))

(def string-test-pairs
  [["" "0:"]
   ["hello" "5:hello"]
   ["goodbye" "7:goodbye"]
   ["hello world" "11:hello world"]
   ["1-5%3~]+=\\| []>.,`??" "20:1-5%3~]+=\\| []>.,`??"]
   ])

(def integer-test-pairs
  [[0 "i0e"]
   [5 "i5e"]
   [-5 "i-5e"]
   [1234567890 "i1234567890e"]
   [-1234567890 "i-1234567890e"]

   ; Javascript Number/MAX_SAFE_INTEGER
   [9007199254740991 "i9007199254740991e"]
   ; Javascript Number/MIN_SAFE_INTEGER
   [-9007199254740991 "i-9007199254740991e"]

#?@(:clj
; CLJS doesn't support very large numbers. Beware!
   [[123456789012345678901234567890123456789012345678901234567890
    "i123456789012345678901234567890123456789012345678901234567890e"]
   [-123456789012345678901234567890123456789012345678901234567890
    "i-123456789012345678901234567890123456789012345678901234567890e"]])
   ])

(def list-test-pairs
  [[(list) "le"]
   [(list "abra" "cadabra") "l4:abra7:cadabrae"]
   [(list (list "list" "of" "lists") (list "like" "omygawd!")) "ll4:list2:of5:listsel4:like8:omygawd!ee"]
   [(list "spam" "eggs") "l4:spam4:eggse"]
   ])

(def map-test-pairs
  [[{} "de"]
   [{"cow" "moo" "spam" "eggs"} "d3:cow3:moo4:spam4:eggse"]
   [{"cow" "moo" "dog" "bark"} "d3:cow3:moo3:dog4:barke"]
   [{"dog" "bark" "cow" "moo"} "d3:cow3:moo3:dog4:barke"]
   ; Bencoded order of keys requires raw byte sorting
   [{"first" "first" "2ace" "second" "3ace" "third"} "d4:2ace6:second4:3ace5:third5:first5:firste"]
   [{"Goodbye" {"maps" "that don't work" "number" 100}}
    "d7:Goodbyed4:maps15:that don't work6:numberi100eee"]
   [{"publisher" "bob" "publisher-webpage" "www.example.com" "publisher.location" "home"}
    "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee"]
   ])

(def mixed-use-pairs
  [[(list 0 "heterogeneous" -5 "lists" 10 {"map" "well"}) "li0e13:heterogeneousi-5e5:listsi10ed3:map4:wellee"]
   [{"hello" (list "world!" "gaia!" "mother earth!") "Goodbye" {"maps" "that don't work" "number" 100}}
    "d7:Goodbyed4:maps15:that don't work6:numberi100ee5:hellol6:world!5:gaia!13:mother earth!ee"]
   [{"hello" (list "world!" "gaia!" "mother earth!")}
    "d5:hellol6:world!5:gaia!13:mother earth!ee"]
   [{"spam" (list "a" "b")} "d4:spaml1:a1:bee"]

   ; DHT test strings
   [{"t" "aa" "y" "q" "q" "ping" "a" {"id" "abcdefghij0123456789"}}
    "d1:ad2:id20:abcdefghij0123456789e1:q4:ping1:t2:aa1:y1:qe"]
   [{"t" "aa" "y" "q" "q" "find_node" "a" {"id" "abcdefghij0123456789" "target" "mnopqrstuvwxyz123456"}}
    "d1:ad2:id20:abcdefghij01234567896:target20:mnopqrstuvwxyz123456e1:q9:find_node1:t2:aa1:y1:qe"]
   [{"t" "aa" "y" "q" "q" "get_peers" "a" {"id" "abcdefghij0123456789" "info_hash" "mnopqrstuvwxyz123456"}}
    "d1:ad2:id20:abcdefghij01234567899:info_hash20:mnopqrstuvwxyz123456e1:q9:get_peers1:t2:aa1:y1:qe"]
   [{"t" "aa" "y" "r" "r"  {"id" "abcdefghij0123456789" "token" "aoeusnth" "values"  ["axje.u" "idhtnm"]}}
    "d1:rd2:id20:abcdefghij01234567895:token8:aoeusnth6:valuesl6:axje.u6:idhtnmee1:t2:aa1:y1:re"]
   ])

(def serialize-edge-cases
  [[-0 "i0e"]
   [005 "i5e"]
   [-005 "i-5e"]

   ; Keywords are coerced to strings
   [:hello "5:hello"]
   [:complex-keyword.spam "20:complex-keyword.spam"]
   ; Namespaced keywords are not preserved
   [::hello "5:hello"]
   [:namespace/hello "5:hello"]
   [:namespace.is.very.long/hello "5:hello"]

   ; Vectors are coerced to lists
   [["abra" "cadabra"] "l4:abra7:cadabrae"]
   [[["list" "of" "lists"] ["like" "omygawd!"]] "ll4:list2:of5:listsel4:like8:omygawd!ee"]
   [[0 "heterogeneous" -5 "lists" 10 {"map" "well"}] "li0e13:heterogeneousi-5e5:listsi10ed3:map4:wellee"]
   [[] "le"]

   ; Map keys are coerced to strings
   [{:cow "moo" :dog "bark"} "d3:cow3:moo3:dog4:barke"]
   [{:dog "bark" :cow "moo"}  "d3:cow3:moo3:dog4:barke"]
   ; You're really gonna hurt if you rely on namepsace keywords. I hurt writing these tests!
   [{::dog "bark" ::cow "moo"}  "d3:cow3:moo3:dog4:barke"]
   [{:namespace/dog "bark" :namespace/cow "moo"}  "d3:cow3:moo3:dog4:barke"]
   [{:first "first" :2ace "second" :3ace "third"} "d4:2ace6:second4:3ace5:third5:first5:firste"]
   ])

(def sym nil)

(def illegal-serialize-arguments
  [nil

   5.4
   -5.4
   (list 5.4)
   {"key" 5.4}
   {"good-val" "string" "key-bad-val" 5.4}
   {"key-bad-val" 5.4 "good-val" "string"}
#?@(:cljs
; The MAX_VALUE and MIN_VALUE Numbers in Javascript are floating point values
   [(.-MAX_VALUE js/Number)
    (.-MIN_VALUE js/Number)])

   ; nil is not supported
   {nil 2}
   {"key" nil}
   (list nil)
   (list 1 2 3 nil)
   {"valid" "val" nil "val"}

   ; Symbols not supported
   {'sym 2}
   {"key" 'sym}
   (list 'sym)
   (list 1 2 3 'sym)
   {"valid" "val" 'sym "val"}

   {1 2}
   {3.3 4}
   {(list :key :omg!) "valid string"}
   {{:map :key? :thats :crazy!} "valid string"}
   {[:vector :as :key] "valid string"}
   {"valid" "val" (list :bad :key :last) "val"}
   {"valid" "val" {:map :key? :thats :crazy!} "val"}
   ])

(def illegal-deserialize-arguments
  [nil

   "i-0e"
   "i09e"
   "i-09e"
   "i-0123e"
   "i-00123e"
   "i0123e"
   "i00123e"
   "i12-345"
   "i-12-345"
   "i-1"
   "i1"
   "i12345ei10e5:eoeoee"
   "i-12345ei10e5:eoeoee"

   ":hello"
   "-5:hello"
   "-5:"
   "5:"
   "5:hello5:hello"
   "5:helloi10e"
   "10:hello"
   "10:hello5:hello"
   "10:helloi0e"
   "10:helloi123456789e"

   "l"
   "lsde"
   "li10e5hello"
   "l10:helloi123456789ee"
   "l10:helloi123456789e5:helloe"
   "l5:helloi123456789e10:helloe"
   "l5:hello5:worldei10e"

   "d"
   "duuuuure"
   "d5:hello5:world"
   "d10:helloi123456789ee"
   "d5:helloi123456789e5:helloe"
   "d5:hello5:worldei10e"
   ])

(defn- run-test-on-pair
  [f base test]
  (testing (str "Base: " base "\n" "Test: " test)
    (is (= test (f base)))))

;; Serialization tests

(deftest serialize-test-pairs
  (doseq [[base test]
          (concat string-test-pairs
                  integer-test-pairs
                  list-test-pairs
                  map-test-pairs
                  mixed-use-pairs
                  serialize-edge-cases)]
    (run-test-on-pair serialize base test)))

(deftest serialize-bad-inputs
  (doseq [arg illegal-serialize-arguments]
    (testing (str "Bad Input: " arg)
      (is (nil? (serialize arg))))))

;; Deserialization tests

(deftest deserialize-test-pairs
  (doseq [[test base]
          (concat integer-test-pairs
                  string-test-pairs
                  mixed-use-pairs
                  map-test-pairs)]
    (run-test-on-pair deserialize base test)))

; BEncoded lists don't define the sort order. Set comparison is used to verify
; list contents while ignoring list order.
(deftest deserialize-unordered-list-pairs
  (doseq [[test base] list-test-pairs]
    (testing (str "Base: " base "\n" "Test: " test)
      (is (= (set test) (set (deserialize base)))))))

(deftest deserialize-bad-inputs
  (doseq [arg illegal-deserialize-arguments]
    (testing (str "Bad Input: " arg)
      (is (nil? (deserialize arg))))))

#?(:cljs
   (do
     (enable-console-print!)
     ; run-tests can be run in a Clojure REPL as well.
     (run-tests)))

(comment
  (time (dotimes [_ 100000000] (map (comp serialize first) mixed-use-pairs)))
  (time (dotimes [_ 100000000] (map (comp deserialize second) mixed-use-pairs)))
  )
