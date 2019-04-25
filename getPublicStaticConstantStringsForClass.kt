@JvmStatic
fun getPublicStaticConstantStringsForClass(TAG:String, clazz:Class<*>) {
    for (f in clazz.declaredFields.filter {
            Modifier.isStatic(it.modifiers) &&
            Modifier.isPublic(it.modifiers) &&
            Modifier.isFinal(it.modifiers)}) {
        if (f.type == String::class.java) {
            try {
                Log.i(TAG, "${clazz.simpleName}.${f.name} = " +
                    f.get(Any()).toString())
            } catch (iae: IllegalAccessException) {
                Log.e(TAG, "${iae.javaClass.simpleName} " +
                    "when trying to read field \"" + f.name + "\": " + iae.message)
            }

        } else if (f.type == Array<String>::class.java) {
            try {
                Log.i(TAG, "${clazz.simpleName}.${f.name} = "+
                    Arrays.toString(f.get(Any()) as Array<String>))
            } catch (iae: IllegalAccessException) {
                Log.e(TAG, iae.javaClass.simpleName +
                    " when trying to read field \"" + f.name + "\": " + iae.message)
            }
        }
    }
}
