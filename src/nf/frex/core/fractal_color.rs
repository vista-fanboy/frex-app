#pragma version(1)
#pragma rs java_package_name(nf.frex.core)

int* colorPalette;
int numColors;
float colorGain;
float colorOffset;
uchar repeatColors;

void root(const float *v_in, int *v_out)
{
    if (*v_in >= 0.0f) {
        const int numColors2 = 2 * numColors;
        int colorIndex = (int) ((numColors * colorGain) * (*v_in) + colorOffset);
        if (repeatColors) {
            colorIndex = colorIndex % numColors2;
            if (colorIndex >= numColors) {
                colorIndex = numColors2 - colorIndex - 1;
            }
        } else {
            if (colorIndex >= numColors) {
                colorIndex = numColors - 1;
            }
        }
        *v_out = colorPalette[colorIndex];
    } else {
        *v_out = 0;
    }
}
