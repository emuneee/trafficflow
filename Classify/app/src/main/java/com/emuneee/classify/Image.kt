package com.emuneee.classify

/**
 * Created by evan on 6/4/17.
 */
data class Image(var filename: String, var isCongested: Int) {

    constructor() : this("", -1)

}