(ns net.joelane.concepts.rps-sync)

(def moves ["rock" "paper" "scissors"])

(defn make-a-move []
  (keyword (first (shuffle moves))))

(defn rule [{p1 :player m1 :move}
            {p2 :player m2 :move}]
  (case m1 :rock (case m2 :rock nil
                          :paper p2
                          :scissors p1)
           :paper (case m2 :rock p1
                           :paper nil
                           :scissors p2)
           :scissors (case m2 :rock p2
                              :paper p1
                              :scissors nil)))

(defn player
  []
  {:player (keyword (gensym "p"))
   :move (make-a-move)})

(defn judge
  [hand-1 hand-2]
  {:winner (rule hand-1 hand-2)
   :h1     hand-1
   :h2     hand-2})

(defn play
  []
  (let [h1 (player)
        h2 (player)
        result (judge h1 h2)]
    (println result)))

(play)


