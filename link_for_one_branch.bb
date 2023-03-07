(require '[babashka.curl :as curl])
(require '[cheshire.core :as json])

(def API-KEY (System/getenv "INPUT_SHORTCUT-API-KEY"))
(def PR-NUMBER (System/getenv "INPUT_PR-NUMBER"))
(def NETLIFY-URL (System/getenv "INPUT_NETLIFY-URL"))
(def URL "https://api.app.shortcut.com/api/v3")
(def HEADERS {:headers {"Content-Type" "application/json"
                        "Shortcut-Token" API-KEY}})

(defn grab-all-started-stories
  []  
  (curl/post "https://api.app.shortcut.com/api/v3/stories/search" (assoc HEADERS 
                                                                            :body (json/generate-string {:archived false
                                                                                                         :workflow_state_types ["started"]})
                                                                            :throw false
                                                                            :follow-redirects false))) 

(defn retrieve-individual-story
  [id]
  (-> (curl/get (str URL "/stories/" id) HEADERS)
      :body
      json/parse-string
      (select-keys ["id" "pull_requests" "external_links"])
      (update "pull_requests" (fn [prs] (map #(get % "number") prs)))
      (update "pull_requests" set)))        
      
(defn add-external-link
  [pr]
  (let [id (get pr "id")
        external-links (set (get pr "external_links"))
        to-add (set/union external-links (set [NETLIFY-URL]))
        url (str URL "/stories/" id)]
    (println "UPDATING -- " url "| ADDING -- " (vec to-add))
    (curl/put url
               (assoc HEADERS :body (json/generate-string {:external_links (vec to-add)})))))

(->> (grab-all-started-stories)
     :body
     json/parse-string
     (map #(get % "id"))
     (map retrieve-individual-story)
     (filter #(contains? (get % "pull_requests") (Integer/parseInt PR-NUMBER)))
     first
     add-external-link)
