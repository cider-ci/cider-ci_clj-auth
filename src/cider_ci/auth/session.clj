; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.auth.session
  (:require
    [cider-ci.utils.debug :as debug]
    [cider-ci.utils.with :as with]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [clojure.java.jdbc :as jdbc]
    [pandect.core :as pandect]
    )
  (:use 
    [clojure.walk :only [keywordize-keys]]
    [clojure.string :only [split]]
    [cider-ci.auth.shared :only [decode-base64]]
    ))



(defonce conf (atom nil))

;### Debug ####################################################################

(defn get-user [user-id]
  (first (jdbc/query (:ds @conf)
              ["SELECT * FROM users 
                WHERE id= ?::UUID" user-id])))

(defn compute-signature [message secret1 secret2]
  (-> message 
      (pandect/sha1-hmac secret1)
      (pandect/sha1-hmac secret2)))

(defn authenticate-session-cookie [request handler]
  (if-let [services-cookie (-> request keywordize-keys :cookies :cider-ci_services-session :value)]
    (try (logging/debug services-cookie)
         (let [[cookie-message cookie-signature] (split services-cookie #"-")
               user-id (decode-base64 cookie-message)
               user (get-user user-id)
               signature (compute-signature cookie-message
                                            (:password_digest user) 
                                            (-> @conf :session :secret))]
           (if (= cookie-signature signature)
             (handler (assoc request :authenticated-user user))
             (throw (IllegalStateException. "Cookie validation failed."))))
         (catch Exception e
           (logging/warn e)
           (handler request)
           ))
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (authenticate-session-cookie request handler)))


;### Debug ####################################################################

(defn initialize [new-conf]
  (reset! conf new-conf))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)

