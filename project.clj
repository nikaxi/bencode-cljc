(defproject com.jamesleonis/bencode-cljc "0.1.0-SNAPSHOT"
  :description "A functional Clojure(script) BEncode serialization library."
  :url "https://github.com/jamesleonis/bencode-cljc"

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :aliases
  {"cljs-test" ["cljsbuild" "test" "unit-tests"]
   "test-all" ["do" "clean," "test," "cljsbuild" "test" "unit-tests"]
   "cljs-auto-test" ["cljsbuild" "auto" "tests"]}

  :cljsbuild
  {:test-commands {"unit-tests" ["node" "target/unit-tests.js"]}
   :builds
   {:tests
    {:source-paths ["src" "test"]
     :notify-command ["node" "target/unit-tests.js"]
     :compiler {:output-to "target/unit-tests.js"
                :optimizations :simple
                :target :nodejs
                :hashbang false
                :main bencode-cljc.core-test}}
    :prod
    {:source-paths ["src"]
     :compiler {:output-to "target/bencode-cljc.js"
                :output-dir "target/cljsbuild/main"
                :optimizations :advanced}}}})
