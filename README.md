# bencode-cljc

> "Instead of cursing the darkness, light a candle."
> ― Benjamin Franklin

A functional Clojure(script) BEncode serialization library.

## Why?

`bencode-cljc` aims to be a portable implementation of the BEncode spec, to boldly go wherever Clojure goes.

## Usage

### Quickstart

```clojure
(ns your.awesome.app
  (:require [bencode-cljc :refer [serialize deserialize]]))

(def out (serialize {"spam" (list "a" "b")})
; => "d4:spaml1:a1:bee"

(deserialize out)
; => {"spam" '("a" "b")}
```
### Details

`serialize` operates on Clojure data structures and outputs a Clojure string.

`deserialize` operates on the opposite of `serialize`.

If an error is encountered in either, `nil` is returned.

#### Encoding

As an encoder, `bencode-cljc` is fairly strict. The aim is full compatibility with existing BEncoded services, and not a fully featured transport encoder. As such, only Integer, Maps, Lists, and Strings are supported.

This comes with some unfortunate side-effects:

* `nil` values are unsupported.
  * Yep, let your outrage flow through you.
  * The BEncode spec does not implement an encoding for `nil`/`null` values, so they are treated as a malformed input.
* Sets are not allowed.
* `float`, `decimal`, and other floating-point numbers are not allowed
* `map` keys are coerced to strings. Complex values (lists, maps) and numbers are not allowed as keys.

But I'm not an evil taskmaster. I bend.

* Map keys can be keywords. However they are converted to strings via `name`. This truncates the namespace from namespaced keywords.
  * A future improvement might allow for deserializing map keys to keywords.
* Vectors are automatically converted to lists.

#### Decoding

In keeping with a portable implementation, `deserialize` operates on vanilla Clojure strings.

Contrary to the [Wikipedia][wiki] entry, BEncode does *not* have the bijection property. List item order is undefined \[[1][theory]\] \[[2][btorg]\]. Beware comparing either serialized or deserialized values.

## Development

The test suite is built in CLJC, and is run under both Clojure and Clojurescript.

The Clojurescript unit tests require NodeJS to run. Otherwise only Leiningen is required.

* `lein test` runs the tests under Clojure.
* `lein cljs-test` runs the tests under Clojurescript and NodeJS.
* `lein test-all` runs both Clojure and Clojurescript test suite.
* `lein cljs-auto-test` automatically compiles and runs the Clojurescript tests on every change.
* The Clojure REPL can run the unit tests as well by calling `(run-tests)` in the `bencode-cljc.core-test` namespace.

## License

Copyright © 2017 James Leonis

Distributed under the EPLv2 license. See LICENSE file.

[wiki]: https://en.wikipedia.org/wiki/Bencode#Features_&_drawbacks
[theory]: https://wiki.theory.org/index.php/BitTorrentSpecification#Bencoding
[btorg]: http://www.bittorrent.org/beps/bep_0003.html#bencoding
