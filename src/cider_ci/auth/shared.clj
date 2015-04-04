; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.auth.shared
  (:require 
    [cider-ci.utils.with :as with]
    [clojure.data.codec.base64 :as base64]
    ))

(defn decode-base64
  [^String string]
  (with/suppress-and-log-warn
    (apply str (map char (base64/decode (.getBytes string))))))


;(decode-base64 "eA==")
