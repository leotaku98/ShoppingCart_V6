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
}
