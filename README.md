# CountMeIn

CountMeIn is a fast counter implementation written in Clojure that can process million of items in a very short period of time and with low memory consumption. Once configured, it can count over lists of items very fast, grouping them by specific attributes and optionally applying "rules" to what can be counted in the form of filters. When available it takes advantage of multiple cores to make calculation even faster.

## FAQ
* For what can I use it?
* CountMeIn is useful on all those scenarios where a large amount of data should be analyzed quickly, for example voting competitions where results are needed as fast as possible. In general CountMeIn can be used to analyzed data in close to real-time scenarios where the delay to setup and run paralelisation over multiple nodes is not feasible, either for accuracy or for latency problems.
* What formats are supported?
* WIP: will supports csv or EDN type file
* What filters are available?
* CountMeIn can filter items that where seen more than a given threshold, or filter by the presence of a specific attribute you can configure

## Counting Bloom Filter

Hadoop core comes with an implementation of a CountingBloomFilter whose complete package name is:

    org.apache.hadoop.util.bloom.CountingBloomFilter

The bloom filter has been used to determine users over-voting on candidates above the predefined threshold.
For a Java example of how to use it use the example below:

    String s="test";
    byte[] nn=new byte[100];
    nn=s.getBytes();
    System.out.println(nn);
    Key mine=new Key(nn);
    CountingBloomFilter cbf=new CountingBloomFilter();
    cbf.add(mine);
    System.out.print(cbf.toString());
    System.out.println("\nmembership:"+cbf.membershipTest(mine));

Or more clojuresquealy:

    (import (org.apache.hadoop.util.bloom CountingBloomFilter Key))
    (def bloom (CountingBloomFilter. 100 10 1))
    (. bloom add (Key. (. "blah" getBytes)))
    (. bloom membershipTest (Key. (. "blah" getBytes)))
    (. bloom approximateCount (Key. (. "blah" getBytes)))

The sources can be found in *hadoop-svn*/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/util/bloom

## Stateful Java objects and Clojure

The counting bloom filter is an example of java object instance with internal state (that is, the bit array to stored the hash of the item). For all the practial purposes this can be considered an external non-transactional non-concurrent database. From that point of view it doesn't make a lot of difference to wrap the bloom filter in a ref, atom or agent since all of the clojure concurreny models assume pure functions that can be re-played from an initial state as many times as possible. One possible solution considering we have the sources, is to use a clojure STM structure for the bit map inside the bloom. Another would be to actually put the good old synchronize block around. But possibly the best solution is to wrap modifying operations around the filter around a Clojure concurrency contract but always change the filter by cloning it first:

    (def current (org.apache.hadoop.util.bloom.CountingBloomFilter. 10 5 0))
    (def next (org.apache.hadoop.util.bloom.CountingBloomFilter. 10 5 0))
    (. current add (org.apache.hadoop.util.bloom.Key. (. "key-1" getBytes)))
    (. next or current)

The last OR operation effectively makes "next" the same as "current" making it possible for the entire transaction to repeat from scratch every time.

## How to run the tests
The project uses [Midje](https://github.com/marick/Midje/). Here are some useful commands:

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.

## Todo
- bloom
  - Extract out the bloom from the hadoop sources to avoid the dependency
  - need a way to extract vectorsize, hashnumber and hashtype out from an existing bloom to clone another one. Fields are protected, but you can write them on a java.io.DataOut and read them later.

### Done
