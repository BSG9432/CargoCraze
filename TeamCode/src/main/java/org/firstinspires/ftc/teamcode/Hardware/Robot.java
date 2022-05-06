package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;

//NOTE: This is off the top of my head

public class Robot {
  //declare hardware variables

  //for the drive train
  public DcMotor frontLeft;
  public DcMotor frontRight;
  public DcMotor backLeft;
  public DcMotor backRight;

  //intake system
 public DcMotor arm;
 public DcMotor intake;
 public DcMotor extend;

  //variables to use IMU's
  public BNO055IMU imu;
  public double imuAngle;
  public double dtSpeed = 1;
  public static Orientation angles;
  public Acceleration gravity;
  public static Telemetry telemetry;
  public GyroSensor gyro;

    //constructor
  public Robot() {

  }

  public void init(HardwareMap hMap) {
    //drive train
    frontLeft = hMap.dcMotor.get("frontLeft");
    backLeft = hMap.dcMotor.get("backLeft");
    frontRight = hMap.dcMotor.get("frontRight");
    backRight = hMap.dcMotor.get("backRight");


    frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

    frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

    //intake system
   arm = hMap.dcMotor.get("arm");
   extend = hMap.dcMotor.get("extend");
   intake = hMap.dcMotor.get("intake");


    //Telemetry to show on phone to confirm that initialization occurred
    //telemetry.addLine("We done bois");//DS
    //Lines that show up in the internal log (can be accessed on the phone
    //Log.d("#BSG", "Started Encoders");
    //Log.d("#ROBOTSTUFF", "Robot Initalized");//Internal Log
  }

  //to initialize the IMU
  public void initIMU(HardwareMap hMap) {

    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

    parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
    parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
    parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
    parameters.loggingEnabled = true;
    parameters.loggingTag = "IMU";
    parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

    // Retrieve and initialize the IMU
    imu = hMap.get(BNO055IMU.class, "imu");
    imu.initialize(parameters);

    // Set up our telemetry dashboard
    composeTelemetry();

  }

  public void composeTelemetry() {

    // At the beginning of each telemetry update, grab a bunch of data
    // from the IMU that we will then display in separate lines.
    telemetry.addAction(() -> {
      // Acquiring the angles is relatively expensive; we don't want
      // to do that in each of the three items that need that info, as that's
      // three times the necessary expense.
      angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
      gravity  = imu.getGravity();
    });

    telemetry.addLine()
            .addData("status", new Func<String>() {
              @Override public String value() {
                return imu.getSystemStatus().toShortString();
              }
            })
            .addData("calib", new Func<String>() {
              @Override public String value() {
                return imu.getCalibrationStatus().toString();
              }
            });

    telemetry.addLine()
            .addData("x", new Func<String>() {
              @Override public String value() {
                return formatAngle(angles.angleUnit, angles.firstAngle);
              }
            })
            .addData("y", new Func<String>() {
              @Override public String value() {
                return formatAngle(angles.angleUnit, angles.secondAngle);
              }
            })
            .addData("z", new Func<String>() {
              @Override public String value() {
                return formatAngle(angles.angleUnit, angles.thirdAngle);
              }
            });

    telemetry.addLine()
            .addData("grvty", new Func<String>() {
              @Override public String value() {
                return gravity.toString();
              }
            })
            .addData("mag", new Func<String>() {
              @Override public String value() {
                return String.format(Locale.getDefault(), "%.3f",
                        Math.sqrt(gravity.xAccel*gravity.xAccel
                                + gravity.yAccel*gravity.yAccel
                                + gravity.zAccel*gravity.zAccel));
              }
            });
  }

  String formatAngle(AngleUnit angleUnit, double angle) {
    return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
  }

  String formatDegrees(double degrees){
    return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
  }

  public void resetAngle()
  {
    imuAngle = 0;
  }

  public double getHeading() {
    angles = imu.getAngularOrientation(AxesReference.INTRINSIC,
            AxesOrder.ZYX, AngleUnit.DEGREES);
    double heading = angles.firstAngle;
    return heading;
  }

  //other functions
  public void drive(double power) {
    drive(power, power, power, power);
  }
  public void drive(double fL, double fR, double bL, double bR){
      frontLeft.setPower(fL* dtSpeed);
      backLeft.setPower(bL* dtSpeed);
      frontRight.setPower(fR* dtSpeed);
      backRight.setPower(bR* dtSpeed);
  }

  public void stopMotors() {
    extend.setPower(0);
    arm.setPower(0);
    //intake.setPower(0);
  }
  public void stopWheels(){
      frontLeft.setPower(0);
      backLeft.setPower(0);
      frontRight.setPower(0);
      backRight.setPower(0);
  }

 /* public void foundationDown() {
    rightFoundation.setPosition(.2);
    leftFoundation.setPosition(.8);
  }

  public void foundationUp() {
    rightFoundation.setPosition(1);
    leftFoundation.setPosition(0);
  }
*/
  public void strafeLeft(long time) {
    frontRight.setPower(.4);
    backRight.setPower(-.4);
    frontLeft.setPower(-.4);
    backLeft.setPower(.4);
  }

  public void strafeRight(long time) {
    frontRight.setPower(-.4);
    backRight.setPower(.4);
    frontLeft.setPower(.4);
    backLeft.setPower(-.4);
  }
}
