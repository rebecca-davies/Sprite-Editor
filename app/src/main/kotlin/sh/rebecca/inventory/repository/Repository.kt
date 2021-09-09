package sh.rebecca.inventory.repository



interface Repository<T> {

    fun findById(id: Int): T?
    fun getCount(): Int
}
