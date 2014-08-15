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


;### Debug ####################################################################

(defn initialize [new-conf]
  (reset! conf new-conf)
  (session/initialize @conf)
  (http-basic/initialize @conf))



