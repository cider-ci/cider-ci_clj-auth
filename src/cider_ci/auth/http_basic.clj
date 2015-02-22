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
    [pandect.algo.sha1 :refer [sha1-hmac]]
    )
  (:use
    [cider-ci.auth.shared]
    [clojure.string :only [lower-case]]
    )
  (:import 
    [bcrypt_jruby BCrypt]
    ))

(def ^:dynamic get-conf (fn [] {}))

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

(defn authenticate-user [login-or-email password]
  (when-let [user (get-user login-or-email)]
    (when (BCrypt/checkpw password  (:password_digest user))
      user)))

(defn password-matches [password username]
  (or (= password (-> (get-conf) :basic_auth  :password))
      (= password (sha1-hmac username (:secret (get-conf))))))

;(sha1-hmac "DemoExecutor" "secret")

(defn get-executor [executor-name]
  (first (jdbc/query 
           (rdbms/get-ds)
           ["SELECT * FROM executors WHERE name = ?" executor-name]
           )))

(defn authenticate-executor [executor-name password-digest]
  (when (password-matches password-digest executor-name)
    (get-executor executor-name)))

(defn- authenticate-role [request roles]
  (if-let [ba (:basic-auth-request request)]
    (let [{username :username password :password} ba
          request (atom request)]
      (logging/debug [ba,username,password])
      (when (:service roles)
        (when (password-matches password username) 
          (swap! request 
                 (fn [request username]
                   (assoc request :authenticated-service {:username username})) 
                 username)))
      (when (:user roles)
        (when-let [user (authenticate-user username password)]
          (swap! request 
                 (fn [request user]
                   (assoc request :authenticated-user user)) 
                 user))) 
      (when (:executor roles)
        (when-let [executor (authenticate-executor username password)]
          (swap! request 
                 (fn [request executor]
                   (assoc request :authenticated-executor executor)) 
                 executor)))
      @request)))

(defn- authenticate-app-or-user [request]
  (if-let [ba (:basic-auth-request request)]
    (let [{username :username password :password} ba]
      (logging/debug [ba,username,password])
      (if (= password (-> (get-conf) :basic_auth  :password))
        (assoc request :authenticated-service {:username username})
        (if-let [user (authenticate-user username password)]
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
           (assoc request :basic-auth-request {:username name :password password}))
         (catch Exception e
           (logging/warn "Failed to extract basic auth properties")
           request))
    request))

(defn wrap   
  ([handler] ; TODO, DEPRECATED remove 
   (logging/warn " DEPRECATED, add the roles argument to http-basic/wrap ")
   (fn [request]
     (-> request
         extract-and-add-basic-auth-properties
         authenticate-app-or-user
         handler)))
  ([handler roles]
   (fn [request]
     (-> request
         extract-and-add-basic-auth-properties
         (authenticate-role roles)
         handler))))

(defn initialize [get-conf-fn]
  (if (map? get-conf-fn)
    (def ^:dynamic get-conf (fn [] get-conf-fn)) ; TODO, remove with version 3.0.0 
    (def ^:dynamic get-conf get-conf-fn)))

;(initialize {:y 42})
;(initialize (fn [] {:x 7}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)



