package com.github.cpickl.bookstore.adapter.jpa

import org.hibernate.dialect.PostgreSQL10Dialect
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import java.sql.Types

@Suppress("Unused")
class CustomPostgreSQLDialect : PostgreSQL10Dialect() {

    init {
        registerColumnType(Types.BLOB, "bytea")
    }

    override fun remapSqlTypeDescriptor(sqlTypeDescriptor: SqlTypeDescriptor): SqlTypeDescriptor {
        if (sqlTypeDescriptor.sqlType == Types.BLOB) {
            return BinaryTypeDescriptor.INSTANCE
        }
        return super.remapSqlTypeDescriptor(sqlTypeDescriptor)
    }
}
