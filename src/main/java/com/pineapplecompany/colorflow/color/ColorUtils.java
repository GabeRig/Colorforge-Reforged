package com.pineapplecompany.colorflow.color;
public final class ColorUtils {
    
    private static final double REF_X = 95.047;
    private static final double REF_Y = 100.000;
    private static final double REF_Z = 108.883;
    
    private static final double K_L = 1.0;
    private static final double K_C = 1.0;
    private static final double K_H = 1.0;
    public static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        float h = 0;
        float s = max == 0 ? 0 : delta / max;
        float v = max;
        
        if (delta != 0) {
            if (max == rf) {
                h = 60 * (((gf - bf) / delta) % 6);
            } else if (max == gf) {
                h = 60 * (((bf - rf) / delta) + 2);
            } else {
                h = 60 * (((rf - gf) / delta) + 4);
            }
        }
        
        if (h < 0) h += 360;
        
        return new float[]{h, s, v};
    }
    
    public static int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = v - c;
        
        float rf, gf, bf;
        
        if (h < 60) {
            rf = c; gf = x; bf = 0;
        } else if (h < 120) {
            rf = x; gf = c; bf = 0;
        } else if (h < 180) {
            rf = 0; gf = c; bf = x;
        } else if (h < 240) {
            rf = 0; gf = x; bf = c;
        } else if (h < 300) {
            rf = x; gf = 0; bf = c;
        } else {
            rf = c; gf = 0; bf = x;
        }
        
        return new int[]{
            Math.round((rf + m) * 255),
            Math.round((gf + m) * 255),
            Math.round((bf + m) * 255)
        };
    }

    public static double[] rgbToLab(int r, int g, int b) {
        double[] xyz = rgbToXyz(r, g, b);
        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }
    
    private static double[] rgbToXyz(int r, int g, int b) {
        double rf = pivotRgb(r / 255.0);
        double gf = pivotRgb(g / 255.0);
        double bf = pivotRgb(b / 255.0);
        
        double x = rf * 0.4124564 + gf * 0.3575761 + bf * 0.1804375;
        double y = rf * 0.2126729 + gf * 0.7151522 + bf * 0.0721750;
        double z = rf * 0.0193339 + gf * 0.1191920 + bf * 0.9503041;
        
        return new double[]{x * 100, y * 100, z * 100};
    }
    
    private static double[] xyzToLab(double x, double y, double z) {
        double xr = pivotXyz(x / REF_X);
        double yr = pivotXyz(y / REF_Y);
        double zr = pivotXyz(z / REF_Z);
        
        double l = 116 * yr - 16;
        double a = 500 * (xr - yr);
        double bVal = 200 * (yr - zr);
        
        return new double[]{l, a, bVal};
    }
    
    private static double pivotRgb(double n) {
        return n > 0.04045 ? Math.pow((n + 0.055) / 1.055, 2.4) : n / 12.92;
    }
    
    private static double pivotXyz(double n) {
        double epsilon = 0.008856;
        double kappa = 903.3;
        return n > epsilon ? Math.cbrt(n) : (kappa * n + 16) / 116;
    }
    
    public static int[] labToRgb(double l, double a, double b) {
        double y = (l + 16) / 116;
        double x = a / 500 + y;
        double z = y - b / 200;
        
        double x3 = x * x * x;
        double y3 = y * y * y;
        double z3 = z * z * z;
        
        x = (x3 > 0.008856 ? x3 : (x - 16.0 / 116) / 7.787) * REF_X;
        y = (y3 > 0.008856 ? y3 : (y - 16.0 / 116) / 7.787) * REF_Y;
        z = (z3 > 0.008856 ? z3 : (z - 16.0 / 116) / 7.787) * REF_Z;
        
        x /= 100;
        y /= 100;
        z /= 100;
        
        double rf = x *  3.2404542 + y * -1.5371385 + z * -0.4985314;
        double gf = x * -0.9692660 + y *  1.8760108 + z *  0.0415560;
        double bf = x *  0.0556434 + y * -0.2040259 + z *  1.0572252;
        
        rf = rf > 0.0031308 ? 1.055 * Math.pow(rf, 1 / 2.4) - 0.055 : 12.92 * rf;
        gf = gf > 0.0031308 ? 1.055 * Math.pow(gf, 1 / 2.4) - 0.055 : 12.92 * gf;
        bf = bf > 0.0031308 ? 1.055 * Math.pow(bf, 1 / 2.4) - 0.055 : 12.92 * bf;
        
        return new int[]{
            clamp((int) Math.round(rf * 255), 0, 255),
            clamp((int) Math.round(gf * 255), 0, 255),
            clamp((int) Math.round(bf * 255), 0, 255)
        };
    }
    
    public static double deltaE2000(double[] lab1, double[] lab2) {
        double l1 = lab1[0], a1 = lab1[1], b1 = lab1[2];
        double l2 = lab2[0], a2 = lab2[1], b2 = lab2[2];
        
        double c1 = Math.sqrt(a1 * a1 + b1 * b1);
        double c2 = Math.sqrt(a2 * a2 + b2 * b2);
        double cAvg = (c1 + c2) / 2;
        
        double c7 = Math.pow(cAvg, 7);
        double g = 0.5 * (1 - Math.sqrt(c7 / (c7 + Math.pow(25, 7))));
        
        double a1Prime = a1 * (1 + g);
        double a2Prime = a2 * (1 + g);
        
        double c1Prime = Math.sqrt(a1Prime * a1Prime + b1 * b1);
        double c2Prime = Math.sqrt(a2Prime * a2Prime + b2 * b2);
        
        double h1Prime = Math.toDegrees(Math.atan2(b1, a1Prime));
        if (h1Prime < 0) h1Prime += 360;
        
        double h2Prime = Math.toDegrees(Math.atan2(b2, a2Prime));
        if (h2Prime < 0) h2Prime += 360;
        
        double deltaLPrime = l2 - l1;
        double deltaCPrime = c2Prime - c1Prime;
        
        double deltaHPrime;
        if (c1Prime * c2Prime == 0) {
            deltaHPrime = 0;
        } else {
            double dhp = h2Prime - h1Prime;
            if (Math.abs(dhp) <= 180) {
                deltaHPrime = dhp;
            } else if (dhp > 180) {
                deltaHPrime = dhp - 360;
            } else {
                deltaHPrime = dhp + 360;
            }
        }
        
        double deltaHPrimeBig = 2 * Math.sqrt(c1Prime * c2Prime) * 
                                Math.sin(Math.toRadians(deltaHPrime / 2));
        
        double lAvg = (l1 + l2) / 2;
        double cPrimeAvg = (c1Prime + c2Prime) / 2;
        
        double hPrimeAvg;
        if (c1Prime * c2Prime == 0) {
            hPrimeAvg = h1Prime + h2Prime;
        } else if (Math.abs(h1Prime - h2Prime) <= 180) {
            hPrimeAvg = (h1Prime + h2Prime) / 2;
        } else if (h1Prime + h2Prime < 360) {
            hPrimeAvg = (h1Prime + h2Prime + 360) / 2;
        } else {
            hPrimeAvg = (h1Prime + h2Prime - 360) / 2;
        }
        
        double t = 1 - 0.17 * Math.cos(Math.toRadians(hPrimeAvg - 30))
                     + 0.24 * Math.cos(Math.toRadians(2 * hPrimeAvg))
                     + 0.32 * Math.cos(Math.toRadians(3 * hPrimeAvg + 6))
                     - 0.20 * Math.cos(Math.toRadians(4 * hPrimeAvg - 63));
        
        double deltaTheta = 30 * Math.exp(-Math.pow((hPrimeAvg - 275) / 25, 2));
        double cPrimeAvg7 = Math.pow(cPrimeAvg, 7);
        double rc = 2 * Math.sqrt(cPrimeAvg7 / (cPrimeAvg7 + Math.pow(25, 7)));
        
        double lAvgMinus50Sq = Math.pow(lAvg - 50, 2);
        double sl = 1 + (0.015 * lAvgMinus50Sq) / Math.sqrt(20 + lAvgMinus50Sq);
        double sc = 1 + 0.045 * cPrimeAvg;
        double sh = 1 + 0.015 * cPrimeAvg * t;
        double rt = -Math.sin(Math.toRadians(2 * deltaTheta)) * rc;
        
        double lightness = deltaLPrime / (K_L * sl);
        double chroma = deltaCPrime / (K_C * sc);
        double hue = deltaHPrimeBig / (K_H * sh);
        
        return Math.sqrt(lightness * lightness + 
                        chroma * chroma + 
                        hue * hue + 
                        rt * chroma * hue);
    }
    
    public static double deltaE2000Rgb(int r1, int g1, int b1, int r2, int g2, int b2) {
        return deltaE2000(rgbToLab(r1, g1, b1), rgbToLab(r2, g2, b2));
    }
    
    public static int[] hexToRgb(String hex) {
        if (hex == null) return null;
        
        hex = hex.startsWith("#") ? hex.substring(1) : hex;
        
        try {
            if (hex.length() == 3) {
                int r = Integer.parseInt(hex.substring(0, 1), 16) * 17;
                int g = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                int b = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                return new int[]{r, g, b};
            }
            
            if (hex.length() == 6) {
                int r = Integer.parseInt(hex.substring(0, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 4), 16);
                int b = Integer.parseInt(hex.substring(4, 6), 16);
                return new int[]{r, g, b};
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }
    
    public static String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", 
            clamp(r, 0, 255), 
            clamp(g, 0, 255), 
            clamp(b, 0, 255));
    }
    
    public static String intToHex(int color) {
        return rgbToHex((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }
    
    public static int rgbToInt(int r, int g, int b) {
        return (clamp(r, 0, 255) << 16) | (clamp(g, 0, 255) << 8) | clamp(b, 0, 255);
    }
    
    public static int argbToInt(int a, int r, int g, int b) {
        return (clamp(a, 0, 255) << 24) | (clamp(r, 0, 255) << 16) | 
               (clamp(g, 0, 255) << 8) | clamp(b, 0, 255);
    }
     
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    public static double[] lerpLab(double[] lab1, double[] lab2, double t) {
        return new double[]{
            lerp(lab1[0], lab2[0], t),
            lerp(lab1[1], lab2[1], t),
            lerp(lab1[2], lab2[2], t)
        };
    }
    
    private ColorUtils() {}
}