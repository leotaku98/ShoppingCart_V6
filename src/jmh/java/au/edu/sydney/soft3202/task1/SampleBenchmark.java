package au.edu.sydney.soft3202.task1;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class SampleBenchmark {

    @Fork(value=1)
    @Warmup(iterations=1)
    @Measurement(iterations = 1)
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void addItemBenchmark(Blackhole bh) {
        ShoppingBasket sb = new ShoppingBasket();
        sb.addItem("apple", 1);
    }

    @Fork(value=1)
    @Warmup(iterations=1)
    @Measurement(iterations = 1)
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void removeItemBenchmark(Blackhole bh) {
        ShoppingBasket sb = new ShoppingBasket();
        sb.addItem("apple", 1);
        sb.removeItem("apple", 1);
    }

    @Fork(value=1)
    @Warmup(iterations=1)
    @Measurement(iterations = 1)
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void addItemNameBenchmark(Blackhole bh) {
        ShoppingBasket sb = new ShoppingBasket();
        sb.addNewItem("new",0.99);
    }

    @Fork(value=1)
    @Warmup(iterations=1)
    @Measurement(iterations = 1)
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void removeItemNameBenchmark(Blackhole bh) {
        ShoppingBasket sb = new ShoppingBasket();
        sb.removeProduct("apple");
    }

    @Fork(value=1)
    @Warmup(iterations=1)
    @Measurement(iterations = 1)
    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void complexBenchmark(Blackhole bh) {
        ShoppingBasket sb = new ShoppingBasket();
        sb.addNewItem("new",0.99);
        int index = 0;
        while(index < 10){
            sb.addItem("new",1);
        }
        sb.removeProduct("new");
    }
}
