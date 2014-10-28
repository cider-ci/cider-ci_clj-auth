; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.auth.http-basic
  (:require
    [cider-ci.utils.rdbms :as rdbms]
    [cider-ci.utils.debug :as debug]
    [cider-ci.utils.with :as with]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [clojure.java.jdbc :as jdbc]
    )
  (:use
    [cider-ci.auth.shared]
    [clojure.string :only [lower-case]]
    )
  (:import 
    [bcrypt_jruby BCrypt]
    ))


(defonce conf (atom nil))

;### Http Basic Authentication ################################################

(defn get-user [login-or-email]
  (with/suppress-and-log-warn
    (when-let [ds (rdbms/get-ds)]
      (or (first (jdbc/query 
                   ds 
                   ["SELECT users.* FROM users 
                    INNER JOIN email_addresses ON email_addresses.user_id = users.id 
                    WHERE email_addresses.email_address = ?
                    LIMIT 1" (lower-case login-or-email)]))
          (first (jdbc/query 
                   ds
                   ["SELECT * FROM users 
                    WHERE login_downcased = ?
                    LIMIT 1" (lower-case login-or-email)]))))))

(defn authenticated? [login-or-email password]
  (when-let [user (get-user login-or-email)]
    (when (BCrypt/checkpw password  (:password_digest user))
      user)))

(defn- authenticate-app-or-user [request]
  (if-let [ba (:basic-auth-request request)]
    (let [{username :username password :password} ba]
      (logging/debug [ba,username,password])
      (if (= password (-> @conf :basic_auth  :password))
        (assoc request :authenticated-service {:username username})
        (if-let [user (authenticated? username password)]
          (assoc request :authenticated-user user)
          request)))
    request))

(defn- extract-and-add-basic-auth-properties 
  "Extracts information from the \"authorization\" header and
  adds a :basic-auth-request key to the request with the value 
  {:name name :password password}."  
  [request]
  (if-let [auth-header ((:headers request) "authorization")]
    (try (let [decoded-val (decode-base64 (last (re-find #"^Basic (.*)$" auth-header)))
               [name password] (clojure.string/split (str decoded-val) #":" 2)]
           (assoc request :basic-auth-request {:name name :password password}))
         (catch Exception e
           (logging/warn "Failed to extract basic auth properties")
           request))
    request))


(defn wrap   
  "Adds :authenticated-service or :authenticated-user
  to the request-map if either authentication was successful." 
  [handler]
  (fn [request]
    (-> request
        extract-and-add-basic-auth-properties
        authenticate-app-or-user
        handler)))


(defn initialize [new-conf]
  (reset! conf new-conf))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)



