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

Contrary to the [Wikipedia][wiki] entry, BEncode is *not* bijectionable. List item order is undefined \[[1][theory]\] \[[2][btorg]\]. Beware comparing either serialized or deserialized values.

## Development

The unit tests require NodeJS for the Clojurescript tests.

The test suite is built in CLJC, and is run under both Clojure and Clojurescript.

* `lein test` runs the tests under Clojure.
* `lein cljs-test` runs the tests under Clojurescript and NodeJS
* `lein test-all` runs both Clojure and Clojurescript test suite.

## License

Copyright © 2017 James Leonis

Distributed under the GPLv3 license.

[wiki]: https://en.wikipedia.org/wiki/Bencode#Features_&_drawbacks
[theory]: https://wiki.theory.org/index.php/BitTorrentSpecification#Bencoding
[btorg]: http://www.bittorrent.org/beps/bep_0003.html#bencoding
