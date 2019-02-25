//simulates a traditional 3-statement for-loop in kotlin

fun <T> fur(initializer: () -> T,
            loopCheck:(T) -> Boolean,
            update:(T) -> T,
            loopBody:(T) -> Unit) {
    var index:T = initializer()
    while(loopCheck(index)) {
        loopBody(index)
        index = update(index)
    }
}

//usage:
fur({0}, {it < array.size}, {it+1}) {//rename index variable here : `index ->`
    //do stuff with `it`
}
