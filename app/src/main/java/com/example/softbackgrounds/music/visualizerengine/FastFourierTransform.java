package com.example.softbackgrounds.music.visualizerengine;

public final class FastFourierTransform {

    private FastFourierTransform() {
    }

    public static void transform(float[] real, float[] imaginary) {
        int n = real.length;

        // Bit reversal
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;

            while ((j & bit) != 0) {
                j ^= bit;
                bit >>= 1;
            }

            j ^= bit;

            if (i < j) {
                float temp = real[i];
                real[i] = real[j];
                real[j] = temp;

                temp = imaginary[i];
                imaginary[i] = imaginary[j];
                imaginary[j] = temp;
            }
        }

        // FFT
        for (int length = 2; length <= n; length <<= 1) {

            double angle =
                    -2.0 * Math.PI / length;

            float wReal = (float) Math.cos(angle);
            float wImaginary = (float) Math.sin(angle);

            for (int i = 0; i < n; i += length) {

                float currentReal = 1f;
                float currentImaginary = 0f;

                int half = length >> 1;

                for (int j = 0; j < half; j++) {

                    int even = i + j;
                    int odd = even + half;

                    float oddReal =
                            real[odd] * currentReal
                                    - imaginary[odd] * currentImaginary;

                    float oddImaginary =
                            real[odd] * currentImaginary
                                    + imaginary[odd] * currentReal;

                    real[odd] =
                            real[even] - oddReal;

                    imaginary[odd] =
                            imaginary[even] - oddImaginary;

                    real[even] += oddReal;
                    imaginary[even] += oddImaginary;

                    float nextReal =
                            currentReal * wReal
                                    - currentImaginary * wImaginary;

                    currentImaginary =
                            currentReal * wImaginary
                                    + currentImaginary * wReal;

                    currentReal = nextReal;
                }
            }
        }
    }

    public static float magnitude(
            float real,
            float imaginary
    ) {
        return (float) Math.sqrt(
                real * real +
                        imaginary * imaginary
        );
    }
}
