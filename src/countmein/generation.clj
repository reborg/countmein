(ns countmein.generation)

;;(unfinished make-invalid)
;;  (:use [midje.sweet :only [unfinished]]))

(defn update-map-values [a-map f]
  (into {} (for [[k v] a-map] [k (f v)])))

(defn relative-counts
  "Calculates the relative counts for all the key in the map
  given a requested total count and the percentages for each key"
  [expected-total percentage-map]
  (let [key-count (count percentage-map)
        update-f (fn [map-value] (* (/ map-value 100) expected-total))]
    (update-map-values percentage-map update-f)))

(defn make-invalid
  "Mark n number of votes as invalid. If n not given then
  mark all of the votes in the collection."
  ([votes]
   (make-invalid votes (count votes)))
  ([votes n]
   (let [head (take n votes)
         tail (nthrest votes n)
         invalidated (map #(update-in % [:valid] (fn [value] false)) head)]
     (concat invalidated tail))))

(defn make-honeypot
  "As a quick approach, honeypotting n votes at the end of
  the collection, while the invalidation is done at the head."
  ([votes]
   (make-honeypot votes (count votes)))
  ([votes n]
   (let [ccount (count votes)
         head (take (- ccount n) votes)
         tail (nthrest votes (- ccount n))
         honeypotted (map #(update-in % [:honeypot] (fn [value] "scarybot")) tail)]
     (concat head honeypotted))))

(defn user-frequencies
  "Returns the frequencies of all user ids in the collection of votes. 
  If a threshold t is given, only returns frequencies > t. If threshold not
  given it is assumed to be 15 (15 is the overflow for the counting bloom filter)."
  ([votes]
   (user-frequencies votes 15))
  ([votes t]
   (let [freqs (->> votes (map #(:user %)) (frequencies))]
     (into {} (filter #(> (val %) t) freqs)))))

(defn pprint-map [m] 
  (println "user-id frequencies of counts")
  (doall 
    (for [[k v] m] (println (str "id: " k " count: " v))))
  (println ""))

(defn partition-user-ids
  "Given a collection of votes it partitions user ids based on given params."
  [votes n]
  (map #(update-in % [:user] (fn [value] (str (rand-int n)))) votes))

(defn make-votes
  "creates a list of t number of votes in the form:
  '({:user \"user-1\" :candidate \"candidate-1\"} {} ...)
  with a specific candidate partitioning and validation options"
  ([t]
   (map 
     #(hash-map :user (str "user-" %) :candidate (str "candidate-" "1")) (range t)))
  ([t partitioning] 
   (let [partitioned-counts (relative-counts t partitioning)]
     (partition-user-ids 
       (mapcat #(repeat (last %) 
                        (hash-map 
                          :valid true 
                          :honeypot "" 
                          :user "???" 
                          :candidate (first %))) partitioned-counts) t)))
  ([t partitioning options] 
   (let [invalid-count (:invalid (relative-counts t (hash-map :invalid (:invalid options))))
         honeypotted-count (:honeypot (relative-counts t (hash-map :honeypot (:honeypot options))))
         votes (make-votes t partitioning)]
     (make-honeypot 
       (make-invalid (partition-user-ids votes t) invalid-count) 
       honeypotted-count))))
