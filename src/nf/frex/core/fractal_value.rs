#pragma version(1)
#pragma rs java_package_name(nf.frex.core)

int width;

double ps;
double z0x;
double z0y;

int iterMax;
double bailOut;
bool decorated;
bool juliaMode;
double juliaX;
double juliaY;

float orbitDilation;
float orbitTranslateX;
float orbitTranslateY;
bool orbitTurbulence;
float orbitTurbulenceIntensity;
float orbitTurbulenceScale;

typedef int (*FrexFractalFunction)(const double initX,
                                   const double initY,
                                   const double constX,
                                   const double constY,
                                   float* orbitX,
                                   float* orbitY);

typedef float (*FrexDistanceFunction)(float x, float y);

__inline__ static float STINGS_evaluate(float x, float y);
__inline__ static int MANDELBROT_computeOrbit(double initX, double initY,
                                   double constX, double constY,
                                   float* orbitX, float* orbitY);

__inline__ static float processOrbit(int numPoints, float* orbitX, float* orbitY);


void root(float *v_out, uint32_t x)
{
    if (*v_out < 0.0f) {

        float orbitX[iterMax];
        float orbitY[iterMax];

        double zx = z0x + (x % width) * ps;
        double zy = z0y - (x / width) * ps;

        int iter;
        if (juliaMode) {
            iter = MANDELBROT_computeOrbit(zx, zy, juliaX, juliaY, orbitX, orbitY);
        } else {
            iter = MANDELBROT_computeOrbit(0.0, 0.0, zx, zy, orbitX, orbitY);
        }

        if (decorated) {
            *v_out = processOrbit(iter, orbitX, orbitY);
        }else {
            *v_out = iter < iterMax ? iter : 0.0f;
        }
    }
}



__inline__ static int MANDELBROT_computeOrbit(const double initX,
                                              const double initY,
                                              const double constX,
                                              const double constY,
                                              float* orbitX,
                                              float* orbitY)
{
    double zx = initX;
    double zy = initY;
    double zxx, zyy;
    int iter;
    for (iter = 0; iter < iterMax; iter++) {
        zxx = zx * zx;
        zyy = zy * zy;
        if (zxx + zyy > bailOut) {
            return iter;
        }
        zy = 2.0 * zx * zy + constY;
        zx = zxx - zyy + constX;

        *orbitX = (float) zx;
        orbitX++;
        *orbitY = (float) zy;
        orbitY++;
    }
    return iterMax;
}

__inline__ static float STINGS_evaluate(float x, float y)
{
    return fmin(fabs(x), fabs(y));
}

__inline__ static float processOrbit(int numPoints, float* orbitX, float* orbitY)
{
    if (numPoints == 0) {
        return 0.0f;
    }
    float vicinitySum = 0.0f;
    float distance;
    for (int i = 0; i < numPoints; i++) {
        distance = STINGS_evaluate(*orbitX - orbitTranslateX, *orbitY - orbitTranslateY) / orbitDilation;
        vicinitySum += 1.0f / (1.0f + distance * distance);
        orbitX++;
        orbitY++;
    }
    return vicinitySum;
}
