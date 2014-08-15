; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.auth.scratch
  (:require
    [cider-ci.utils.debug :as debug]
    [cider-ci.utils.with :as with]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [pandect.core :as pandect]
    [clojure.data.json :as json]
    [ring.middleware.cookies :as cookies]
    )
  (:import 
    [org.jruby.embed InvokeFailedException ScriptingContainer]
    ))



(defn run-jruby [ruby-code]
  (.runScriptlet (ScriptingContainer.) ruby-code))

(defn ruby-sha1-hmac [message secret]
  (let [json_aray (json/write-str [message,secret])
        ruby-code (str "require 'openssl'; "
                       "require 'json'; "
                       "message, secret= JSON.parse('" json_aray "'); "
                       "digest = OpenSSL::Digest.new('sha1'); "
                       "OpenSSL::HMAC.hexdigest(digest, secret, message)")]
    (run-jruby ruby-code)))


(= 
  (pandect/sha1-hmac "message" "secret")
  (ruby-sha1-hmac "message" "secret"))

