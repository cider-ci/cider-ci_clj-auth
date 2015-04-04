(defproject cider-ci/clj-auth "3.0.0-beta.1"
  :description "Cider-CI Authentication"
  :url "https://github.com/cider-ci/cider-ci_clj-auth"
  :license {:name "GNU AFFERO GENERAL PUBLIC LICENSE Version 3"
            :url "http://www.gnu.org/licenses/agpl-3.0.html"}
  :dependencies [
                 [cider-ci/clj-utils "3.0.0-beta.2"]
                 [org.clojure/data.codec "0.1.0"]
                 [pandect "0.5.1"]
                 ]
  :source-paths ["src"]
  :java-source-paths ["lib/bcrypt-ruby/ext/jruby"] 

  :repositories [["tmp" {:url "http://maven-repo-tmp.drtom.ch" :snapshots false}]]
  :plugins [[org.apache.maven.wagon/wagon-ssh-external "2.6"]]
  :deploy-repositories [ ["tmp" "scp://maven@schank.ch/tmp/maven-repo/"]]
  )

(cemerick.pomegranate.aether/register-wagon-factory!
  "scp" #(let [c (resolve 'org.apache.maven.wagon.providers.ssh.external.ScpExternalWagon)]
           (clojure.lang.Reflector/invokeConstructor c (into-array []))))
