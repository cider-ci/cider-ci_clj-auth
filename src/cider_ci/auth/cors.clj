; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.auth.cors
  (:require
    [cider-ci.utils.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [ring.util.response :as response]
    ))

(defn wrap [handler]
  "This wrapper adds the Access-Control-Allow-Origin and
  Access-Control-Allow-Credentials headers to the response. The value of the
  Access-Control-Allow-Credentials is based on the first present request header
  cors-origin, origin, pair of scheme and host, or finally '*' if non of the
  previous was found." 
  (fn [request]
    (-> (handler request)
        (response/header 
          "Access-Control-Allow-Origin" (or 
                                          (-> request :headers (get "cors-origin"))
                                          (-> request :headers (get "origin"))
                                          (let [scheme  (-> request :scheme name)
                                                host  (-> request :headers (get "host"))]
                                            (when (and scheme host)
                                              (str scheme "://" host)))
                                          "*"))
        (response/header
          "Access-Control-Allow-Credentials" "true"))))


