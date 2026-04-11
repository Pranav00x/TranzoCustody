package com.tranzo.custody.data.local

import androidx.room.TypeConverter
import com.tranzo.custody.domain.model.Chain

class TranzoTypeConverters {
    @TypeConverter
    fun fromChain(chain: Chain): String {
        return chain.name
    }

    @TypeConverter
    fun toChain(value: String): Chain {
        return Chain.valueOf(value)
    }
}
