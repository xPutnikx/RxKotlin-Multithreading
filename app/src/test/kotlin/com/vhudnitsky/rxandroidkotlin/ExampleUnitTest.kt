package com.vhudnitsky.rxandroidkotlin

import org.junit.Test
import rx.Observable
import rx.Subscriber
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
  @Test
  fun callAllOnMainThread() {
    println("callAllOnMainThread")
    testSchedulersTemplate()
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  @Test
  fun subscribeOnIO() {
    println("subscribeOnIO")
    //all methods on io thread

    Observable.just("Default data")
        .mergeWith(testSchedulersTemplate())

    testSchedulersTemplate()
        .doOnSubscribe{
          logThread("doOnSubscribe")
        }
        .subscribeOn(Schedulers.io())
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  fun getUser(): Observable<String> {
    return testSchedulersTemplate()
        .subscribeOn(Schedulers.io())
  }

  @Test
  fun observeOnIO() {
    println("observeOnIO")
    //all methods below on io thread
    getUser()
        .observeOn(Schedulers.io())
        .map { s ->
          logThread("Map")
          "$s#"
        }
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  @Test
  fun observeOnIOSubscribeOnComputation() {
    println("observeOnIOSubscribeOnComputation")
    //all methods above on computation thread
    //all methods below on io thread
    testSchedulersTemplate()
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.io())
        .map { s ->
          logThread("Map")
          "$s#"
        }
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  @Test
  fun observeOnIOObserveOnComputation() {
    println("observeOnIOObserveOnComputation")
    //all methods below on io thread
    testSchedulersTemplate()
        .observeOn(Schedulers.io())
        .map { s ->
          logThread("1st Map")
          "$s#"
        }
        //all methods below on computation thread
        .observeOn(Schedulers.computation())
        .map { s ->
          logThread("2st Map")
          "$s#"
        }
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  @Test
  fun test3() {
    println("test3")
    //all methods below on io thread
    testSchedulersTemplate()
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.io())
        .map { s ->
          logThread("1st Map")
          "$s#"
        }
        //all methods below on computation thread
        .observeOn(Schedulers.newThread())
        .subscribeOn(Schedulers.computation())
        .map { s ->
          logThread("2st Map")
          "$s#"
        }
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  @Test
  fun subscribeOnIOSubscribeOnComputation() {
    println("subscribeOnIOSubscribeOnComputation")
    //all methods below on io thread
    testSchedulersTemplate()
        .subscribeOn(Schedulers.io())
        .map { s ->
          logThread("1st Map")
          "$s#"
        }
        //all methods below on computation thread
        .subscribeOn(Schedulers.computation())
        .map { s ->
          logThread("2st Map")
          "$s#"
        }
        .subscribe(subscriber)
    subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS)
    subscriber.assertNoErrors()
  }

  private fun testSchedulersTemplate(): Observable<String> {
    return Observable
        .create<String> { subscriber ->
          logThread("Inside observable")
          subscriber.onNext("Hello from observable")
          subscriber.onCompleted()
        }
        .doOnNext({ s ->
                    logThread("Before transform")
                  })

  }

  val subscriber = TestSubscriber(object : Subscriber<String>() {
    override fun onCompleted() {
      logThread("In onComplete")
    }

    override fun onError(e: Throwable) {
    }

    override fun onNext(o: String) {
      logThread("In onNext")
    }
  })

  private fun logThread(s: String) {
    println("${Thread.currentThread()} - $s")
  }

}
