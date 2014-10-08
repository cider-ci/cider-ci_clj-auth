(defproject cider-ci/clj-auth "0.1.5"
  :description "Cider-CI Authentication"
  :url "https://github.com/cider-ci/cider-ci_clj-auth"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE Version 3"
            :url "http://www.gnu.org/licenses/agpl-3.0.html"}
  :dependencies [
                 [clj-logging-config "1.9.12"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [pandect "0.4.0"]
                 [robert/hooke "1.3.0"]
                 ]
  :source-paths ["src"]
  :java-source-paths ["lib/bcrypt-ruby/ext/jruby"] 
  )
