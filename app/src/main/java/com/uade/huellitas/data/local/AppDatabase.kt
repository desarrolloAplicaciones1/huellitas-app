package com.uade.huellitas.data.local

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uade.huellitas.data.local.converter.Converters
import com.uade.huellitas.data.local.dao.AlertDao
import com.uade.huellitas.data.local.dao.PetDao
import com.uade.huellitas.data.local.dao.UserDao
import com.uade.huellitas.data.local.entity.AlertEntity
import com.uade.huellitas.data.local.entity.PetEntity
import com.uade.huellitas.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, PetEntity::class, AlertEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "patitas_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN location TEXT")
                db.execSQL("ALTER TABLE alerts ADD COLUMN size TEXT")
                db.execSQL("ALTER TABLE alerts ADD COLUMN hasCollar INTEGER")
                db.execSQL("ALTER TABLE alerts ADD COLUMN isCastrated INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alerts ADD COLUMN ownerName TEXT")
            }
        }

        // Rebuilds alerts table without ownerId/petId foreign keys so alerts from
        // other users can be inserted even when their owner/pet rows are absent locally.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `alerts_new` (
                        `id` TEXT NOT NULL,
                        `ownerId` TEXT NOT NULL,
                        `ownerName` TEXT,
                        `petId` TEXT,
                        `type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `petName` TEXT NOT NULL,
                        `petType` TEXT NOT NULL,
                        `breed` TEXT,
                        `color` TEXT,
                        `size` TEXT,
                        `hasCollar` INTEGER,
                        `isCastrated` INTEGER,
                        `description` TEXT NOT NULL,
                        `photoUrlsJson` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `address` TEXT,
                        `contactPhone` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `alerts_new`
                    SELECT `id`,`ownerId`,`ownerName`,`petId`,`type`,`status`,`petName`,`petType`,
                           `breed`,`color`,`size`,`hasCollar`,`isCastrated`,`description`,
                           `photoUrlsJson`,`latitude`,`longitude`,`address`,`contactPhone`,
                           `createdAt`,`updatedAt`,`pendingSync`
                    FROM `alerts`
                """.trimIndent())
                db.execSQL("DROP TABLE `alerts`")
                db.execSQL("ALTER TABLE `alerts_new` RENAME TO `alerts`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alerts_ownerId` ON `alerts` (`ownerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alerts_petId` ON `alerts` (`petId`)")
            }
        }
    }
}
