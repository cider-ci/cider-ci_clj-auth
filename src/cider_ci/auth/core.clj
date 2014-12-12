; Copyright (C) 2013, 2014 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns cider-ci.auth.core
  (:require
    [cider-ci.auth.http-basic :as http-basic]
    [cider-ci.auth.session :as session]
    [cider-ci.utils.debug :as debug]
    [cider-ci.utils.with :as with]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    ))


(defonce conf (atom nil))


;##### wrap auth ############################################################## 

(defn- return-authenticate! [request]
  {:status 401
   :headers 
   {"WWW-Authenticate" 
    "Basic realm=\"Cider-CI; sign in or provide credentials\""}
   })

(defn- authenticate-and-authorize-service [request handler] 
  (cond
    (:authenticated-service request) (handler request) 
    :else (return-authenticate! request)))

(defn- authenticate-and-authorize-service-or-user [request handler] 
  (cond
    (:authenticated-user request) (handler request)
    (:authenticated-service request) (handler request) 
    :else (return-authenticate! request)))


;### auth ####################################################################

(defn wrap-authenticate-and-authorize-service-or-user
  "Check for :authenticated-service and pass on, or interrupt and return 401."
  [handler]
  (fn [request]
    (authenticate-and-authorize-service-or-user request handler)))


(defn wrap-authenticate-and-authorize-service 
  "Check for :authenticated-service and pass on, or interrupt and return 401."
  [handler]
  (fn [request]
    (authenticate-and-authorize-service request handler)))




;### Initialize ##############################################################

(defn initialize [new-conf]
  (reset! conf new-conf)
  (session/initialize @conf)
  (http-basic/initialize @conf))



