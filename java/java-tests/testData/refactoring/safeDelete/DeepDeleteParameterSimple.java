class Test {
    void foo(int i) {
        bar(i);
        bar(i);
    }

    void bar(int <caret>i){}
}