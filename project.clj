(defproject com.jamesleonis/bencode-cljc "0.1.1"
  :description "A functional Clojure(script) BEncode serialization library."
  :url "https://github.com/jamesleonis/bencode-cljc"
  :license {:name "Eclipse Public License - v 2.0"
            :url "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt"}

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src"]
  :test-paths ["test"]

  :aliases
  {"cljs-test" ["do" "clean," "cljsbuild" "once" "tests"]
   "test-all" ["do" "clean," "test," "cljsbuild" "once" "tests"]
   "cljs-auto-test" ["cljsbuild" "auto" "tests"]}

  :cljsbuild
  {:test-commands {"unit-tests" ["node" "target/unit-tests.js"]}
   :builds
   {:tests
    {:source-paths ["src" "test"]
     :notify-command ["node" "target/unit-tests.js"]
     :compiler {:output-to "target/unit-tests.js"
                :optimizations :none
                :target :nodejs
                :main bencode-cljc.core-test}}
    :prod
    {:source-paths ["src"]
     :compiler {:output-to "target/bencode-cljc.js"
                :output-dir "target/cljsbuild/main"
                :optimizations :advanced}}}})
