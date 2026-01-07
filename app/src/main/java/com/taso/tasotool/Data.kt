package com.taso.tasotool

import androidx.room.*

@Entity(tableName = "ledger_rows")
data class LedgerRow(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var numbers: String = ""
)

@Dao
interface RowDao {
    @Query("SELECT * FROM ledger_rows")
    fun getAll(): List<LedgerRow>

    @Insert
    fun insert(row: LedgerRow): Long

    @Update
    fun update(row: LedgerRow)

    @Delete
    fun delete(row: LedgerRow)
}

@Database(entities = [LedgerRow::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rowDao(): RowDao
}
