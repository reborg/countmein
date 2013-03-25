(ns countmein.t-bloom
  (:use midje.sweet)
  (:use countmein.bloom))

(facts "checking facts about the bloom"
       (fact "is empty"
             (empty-bloom? (build-bloom [])) => truthy)
       (fact "there something in there"
             (empty-bloom? (build-bloom ["blah"])) => falsey))

(facts "create blooms from maps"
       (fact "empty bloom"
             (bloom-check "asdf" (bloom-in :a [{:user "asdf"} {:nope "a"}])) => falsey)
       (fact "one element in the bloom"
             (bloom-check "asdf" (bloom-in :a [{:a "asdf"} {:nope "a"}])) => truthy))

(facts "safe alter of blooms by cloning a new copy"
       (fact "returns a different bloom"
             (let [bloom (empty-bloom)
                   clone (bloom-add "key" bloom)]
               (= bloom clone) => falsey))
       (fact "the bloom clone was added a new element"
             (let [bloom (empty-bloom)
                   clone (bloom-add "key-1" bloom)]
               (item-count "key-1" clone) => 1
               (item-count "key-1" bloom) => 0)))

(facts "using the bloom filter to filter out elements from a collection"
       (fact "all users are unique, no filtering expected"
             (let [votes [{:user "user1"} {:user "user2"} {:user "user3"}]
                   bloom (empty-bloom 1000 10 0)]
               (filter (confidence-filter 1 bloom) votes) => votes))
       (fact "one user above threshold"
             (let [votes [{:user "user2"} {:user "user2"} {:user "user2"} {:user "user3"}]
                   bloom (empty-bloom 1000 10 0)]
               (filter (confidence-filter 2 bloom) votes) => '({:user "user2"} {:user "user2"} {:user "user3"})))
       (fact "all above"
             (let [votes [{:user "user2"} {:user "user2"} {:user "user3"} {:user "user3"}]
                   bloom (empty-bloom 1000 10 0)]
               (filter (confidence-filter 1 bloom) votes) => '({:user "user2"} {:user "user3"}))))
