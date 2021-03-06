(ns choices.generatejson
  (:require [yaml.core :as yaml]
            [cheshire.core :as json]))

(defn remove-deep [data keys]
  (clojure.walk/prewalk
   (fn [node] (if (map? node)
                (apply dissoc node keys) node))
   data))

(defn -main []
  (let [parsed-config   (yaml/parse-string (slurp "config.yml"))
        tree            (map
                         (fn [branch]
                           (if (re-matches #"^2\.[12]$" (:node branch))
                             (merge branch {:choices "Saisie utilisateur"})
                             branch))
                         (remove-deep (:tree parsed-config) [:color]))
        score-variables (remove-deep (:score-variables parsed-config) [:display])
        conditional-score-outputs
        (remove-deep (:conditional-score-outputs parsed-config)
                     [:notification :priority])]
    (spit "docs/json/variables.json"
          (json/generate-string
           {:variables score-variables}))
    (spit "docs/json/questions.json"
          (json/generate-string
           {:questions (map
                        (fn [n] (select-keys n [:node :text :choices]))
                        (remove #(= (:home-page %) true) tree))}))
    (spit "docs/json/conclusions.json"
          (json/generate-string
           {:conclusions conditional-score-outputs}))
    (println "File config.json generated")))

;; (-main)

