#pragma version(1)
#pragma rs java_package_name(nf.frex.core)

int width;

int iterMax;

double ps;  // region.getPixelSize(width, height);
double z0x; // region.getUpperLeftX(width, ps);
double z0y; // region.getUpperLeftY(height, ps);

double bailOut;
uchar decorated;
uchar juliaMode;
double juliaX;
double juliaY;

double orbitDilation;
double orbitTranslateX;
double orbitTranslateY;
uchar orbitTurbulence;
double orbitTurbulenceIntensity;
double orbitTurbulenceScale;

static float STINGS_evaluate(float x, float y);
static int MANDELBROT_computeOrbit(double initX, double initY,
                                   double constX, double constY,
                                   float* orbitX, float* orbitY);

static float processOrbit(int numPoints, float* orbitX, float* orbitY);

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


//public static final Fractal MANDELBROT = new Fractal(new Region(-0.5, 0.0, 1.2), 100, 100.0) {


static int MANDELBROT_computeOrbit(double initX, double initY,
                                   double constX, double constY,
                                   float* orbitX, float* orbitY)
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

static float STINGS_evaluate(float x, float y)
{
    return fmin(fabs(x), fabs(y));
}

static float processOrbit(int numPoints, float* orbitX, float* orbitY)
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
