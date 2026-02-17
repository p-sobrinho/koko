package dev.koji.koko.common.ktx

import net.minecraft.network.codec.StreamCodec

object StreamCodecKTX {
    fun <B, C, T1, T2, T3, T4, T5, T6> composite(
        streamCodec1: StreamCodec<in B, T1>, method1: (C) -> T1,
        streamCodec2: StreamCodec<in B, T2>, method2: (C) -> T2,
        streamCodec3: StreamCodec<in B, T3>, method3: (C) -> T3,
        streamCodec4: StreamCodec<in B, T4>, method4: (C) -> T4,
        streamCoded5: StreamCodec<in B, T5>, method5: (C) -> T5,
        streamCoded6: StreamCodec<in B, T6>, method6: (C) -> T6,
        constructor: (T1, T2, T3, T4, T5, T6) -> C
    ): StreamCodec<B, C> {
        return object : StreamCodec<B, C> {
            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun encode(bufferObject: B, encodeObject: C) {
                streamCodec1.encode(bufferObject, method1(encodeObject))
                streamCodec2.encode(bufferObject, method2(encodeObject))
                streamCodec3.encode(bufferObject, method3(encodeObject))
                streamCodec4.encode(bufferObject, method4(encodeObject))
                streamCoded5.encode(bufferObject, method5(encodeObject))
                streamCoded6.encode(bufferObject, method6(encodeObject))
            }

            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun decode(decodeObject: B): C {
                val t1 = streamCodec1.decode(decodeObject)
                val t2 = streamCodec2.decode(decodeObject)
                val t3 = streamCodec3.decode(decodeObject)
                val t4 = streamCodec4.decode(decodeObject)
                val t5 = streamCoded5.decode(decodeObject)
                val t6 = streamCoded6.decode(decodeObject)

                return constructor(t1, t2, t3, t4, t5, t6)
            }
        }
    }

    fun <B, C, T1, T2, T3, T4, T5, T6, T7> composite(
        streamCodec1: StreamCodec<in B, T1>, method1: (C) -> T1,
        streamCodec2: StreamCodec<in B, T2>, method2: (C) -> T2,
        streamCodec3: StreamCodec<in B, T3>, method3: (C) -> T3,
        streamCodec4: StreamCodec<in B, T4>, method4: (C) -> T4,
        streamCoded5: StreamCodec<in B, T5>, method5: (C) -> T5,
        streamCoded6: StreamCodec<in B, T6>, method6: (C) -> T6,
        streamCoded7: StreamCodec<in B, T7>, method7: (C) -> T7,
        constructor: (T1, T2, T3, T4, T5, T6, T7) -> C
    ): StreamCodec<B, C> {
        return object : StreamCodec<B, C> {
            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun encode(bufferObject: B, encodeObject: C) {
                streamCodec1.encode(bufferObject, method1(encodeObject))
                streamCodec2.encode(bufferObject, method2(encodeObject))
                streamCodec3.encode(bufferObject, method3(encodeObject))
                streamCodec4.encode(bufferObject, method4(encodeObject))
                streamCoded5.encode(bufferObject, method5(encodeObject))
                streamCoded6.encode(bufferObject, method6(encodeObject))
                streamCoded7.encode(bufferObject, method7(encodeObject))
            }

            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun decode(decodeObject: B): C {
                val t1 = streamCodec1.decode(decodeObject)
                val t2 = streamCodec2.decode(decodeObject)
                val t3 = streamCodec3.decode(decodeObject)
                val t4 = streamCodec4.decode(decodeObject)
                val t5 = streamCoded5.decode(decodeObject)
                val t6 = streamCoded6.decode(decodeObject)
                val t7 = streamCoded7.decode(decodeObject)

                return constructor(t1, t2, t3, t4, t5, t6, t7)
            }
        }
    }

    fun <B, C, T1, T2, T3, T4, T5, T6, T7, T8> composite(
        streamCodec1: StreamCodec<in B, T1>, method1: (C) -> T1,
        streamCodec2: StreamCodec<in B, T2>, method2: (C) -> T2,
        streamCodec3: StreamCodec<in B, T3>, method3: (C) -> T3,
        streamCodec4: StreamCodec<in B, T4>, method4: (C) -> T4,
        streamCoded5: StreamCodec<in B, T5>, method5: (C) -> T5,
        streamCoded6: StreamCodec<in B, T6>, method6: (C) -> T6,
        streamCoded7: StreamCodec<in B, T7>, method7: (C) -> T7,
        streamCoded8: StreamCodec<in B, T8>, method8: (C) -> T8,
        constructor: (T1, T2, T3, T4, T5, T6, T7, T8) -> C
    ): StreamCodec<B, C> {
        return object : StreamCodec<B, C> {
            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun encode(bufferObject: B, encodeObject: C) {
                streamCodec1.encode(bufferObject, method1(encodeObject))
                streamCodec2.encode(bufferObject, method2(encodeObject))
                streamCodec3.encode(bufferObject, method3(encodeObject))
                streamCodec4.encode(bufferObject, method4(encodeObject))
                streamCoded5.encode(bufferObject, method5(encodeObject))
                streamCoded6.encode(bufferObject, method6(encodeObject))
                streamCoded7.encode(bufferObject, method7(encodeObject))
                streamCoded8.encode(bufferObject, method8(encodeObject))
            }

            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun decode(decodeObject: B): C {
                val t1 = streamCodec1.decode(decodeObject)
                val t2 = streamCodec2.decode(decodeObject)
                val t3 = streamCodec3.decode(decodeObject)
                val t4 = streamCodec4.decode(decodeObject)
                val t5 = streamCoded5.decode(decodeObject)
                val t6 = streamCoded6.decode(decodeObject)
                val t7 = streamCoded7.decode(decodeObject)
                val t8 = streamCoded8.decode(decodeObject)

                return constructor(t1, t2, t3, t4, t5, t6, t7, t8)
            }
        }
    }

    fun <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> composite(
        streamCodec1: StreamCodec<in B, T1>, method1: (C) -> T1,
        streamCodec2: StreamCodec<in B, T2>, method2: (C) -> T2,
        streamCodec3: StreamCodec<in B, T3>, method3: (C) -> T3,
        streamCodec4: StreamCodec<in B, T4>, method4: (C) -> T4,
        streamCoded5: StreamCodec<in B, T5>, method5: (C) -> T5,
        streamCoded6: StreamCodec<in B, T6>, method6: (C) -> T6,
        streamCoded7: StreamCodec<in B, T7>, method7: (C) -> T7,
        streamCoded8: StreamCodec<in B, T8>, method8: (C) -> T8,
        streamCoded9: StreamCodec<in B, T9>, method9: (C) -> T9,
        constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> C
    ): StreamCodec<B, C> {
        return object : StreamCodec<B, C> {
            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun encode(bufferObject: B, encodeObject: C) {
                streamCodec1.encode(bufferObject, method1(encodeObject))
                streamCodec2.encode(bufferObject, method2(encodeObject))
                streamCodec3.encode(bufferObject, method3(encodeObject))
                streamCodec4.encode(bufferObject, method4(encodeObject))
                streamCoded5.encode(bufferObject, method5(encodeObject))
                streamCoded6.encode(bufferObject, method6(encodeObject))
                streamCoded7.encode(bufferObject, method7(encodeObject))
                streamCoded8.encode(bufferObject, method8(encodeObject))
                streamCoded9.encode(bufferObject, method9(encodeObject))
            }

            @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE", "JAVA_TYPE_MISMATCH")
            override fun decode(decodeObject: B): C {
                val t1 = streamCodec1.decode(decodeObject)
                val t2 = streamCodec2.decode(decodeObject)
                val t3 = streamCodec3.decode(decodeObject)
                val t4 = streamCodec4.decode(decodeObject)
                val t5 = streamCoded5.decode(decodeObject)
                val t6 = streamCoded6.decode(decodeObject)
                val t7 = streamCoded7.decode(decodeObject)
                val t8 = streamCoded8.decode(decodeObject)
                val t9 = streamCoded9.decode(decodeObject)

                return constructor(t1, t2, t3, t4, t5, t6, t7, t8, t9)
            }
        }
    }
}