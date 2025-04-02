/**Acts like a normal [MutableMap] unless .get(key) would return null,
 * in which case it creates a new value for that key (specified by [newElementToAddWhenMissing]),
 * adds it to the underlying map, then returns it. [get] is thus non-null for this MutableMap.*/
class DefaultCreationMutableMap<K, V>(
    val newElementToAddWhenMissing: (K) -> V
) : MutableMap<K, V> {

    val map = mutableMapOf<K, V>()

    private val TAG = this::class.simpleName

    override val size: Int get() = map.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    override val keys: MutableSet<K> get() = map.keys
    override val values: MutableCollection<V> get() = map.values


    override operator fun get(key: K): V {
        map[key]?.let { return it }

        Log.d(TAG, "tables before size: ${map.size}")
        val newValue = newElementToAddWhenMissing(key)
        map.put(key, newValue)
        return newValue
    }


    override fun clear() = map.clear()
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(value: V): Boolean = map.containsValue(value)
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun remove(key: K): V? = map.remove(key)
    override fun put(key: K, value: V): V? = map.put(key, value)
    override fun putAll(from: Map<out K, V>) = map.putAll(from)
}
