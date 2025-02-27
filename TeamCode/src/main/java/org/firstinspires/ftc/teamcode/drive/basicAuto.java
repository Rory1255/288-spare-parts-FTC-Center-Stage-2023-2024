package org.firstinspires.ftc.teamcode.drive;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.annotation.SuppressLint;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

@Disabled

@TeleOp(name = "basic auto", group = "Linear OpMode")
public class basicAuto extends LinearOpMode {
    private final ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftFrontDriveMotor = null;
    private DcMotor rightFrontDriveMotor = null;
    private DcMotor leftRearDriveMotor = null;
    private DcMotor rightRearDriveMotor = null;
    private DcMotor armExtensionFront = null;
    private DcMotor armExtensionBack = null;
    private DcMotor armHeightMotor = null;
    private DcMotor airplaneMotor = null;

    //servo
    private CRServo leftBackFeed = null;
    private CRServo rightBackFeed = null;
    private CRServo leftFeedServo = null;
    private CRServo rightFeedServo = null;
    private Servo airplaneServo = null;

   /* private double targetHeight = 0;
    private double maxHeight = 2923;
    private double targetLength = 0;
    private double maxLength = 4200;

    private double scorePresetHeight = 500;
    private double climbPresetHeight = 10;
    final double HEIGHT_ADJUST_RATE = 40.0;
    final double LENGTH_ADJUST_RATE = 55.0;*/


    //joystick variables
    final double JOYSTICK_DEAD_ZONE = 0.20;
    final double JOYSTICK_MOVEMENT_SENSITIVITY = 0.75;
    final double JOYSTICK_ROTATION_SENSITIVITY = 1.00;

    private BNO055IMU imu;
    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() {
        //Hardware mapping
        leftFrontDriveMotor = hardwareMap.get(DcMotor.class, "frontLeftDriveMotor");
        rightFrontDriveMotor = hardwareMap.get(DcMotor.class, "frontRightDriveMotor");
        leftRearDriveMotor = hardwareMap.get(DcMotor.class, "backLeftDriveMotor");
        rightRearDriveMotor = hardwareMap.get(DcMotor.class, "backRightDriveMotor");

        armExtensionFront = hardwareMap.get(DcMotor.class, "frontArmExtensionMotor");
        armExtensionBack = hardwareMap.get(DcMotor.class, "backArmExtensionMotor");
        armHeightMotor = hardwareMap.get(DcMotor.class, "armHeightMotor");

        // topIntakeServo = hardwareMap.get(CRServo.class, "topIntakeServo");
        //bottomIntakeServo = hardwareMap.get(CRServo.class, "bottomIntakeServo");
        leftBackFeed = hardwareMap.get(CRServo.class, "leftBackFeed");
        rightBackFeed = hardwareMap.get(CRServo.class, "rightBackFeed");
        leftFeedServo = hardwareMap.get(CRServo.class, "leftFeedServo");
        rightFeedServo = hardwareMap.get(CRServo.class, "rightFeedServo");

        airplaneMotor = hardwareMap.get(DcMotor.class, "airplaneMotor");

        airplaneServo = hardwareMap.get(Servo.class, "airplaneServo");
        //set brake mode
        leftRearDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRearDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFrontDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFrontDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //armHeightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //armExtensionBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //armExtensionFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        //motor directions
        leftFrontDriveMotor.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDriveMotor.setDirection(DcMotor.Direction.REVERSE);
        leftRearDriveMotor.setDirection(DcMotor.Direction.FORWARD);
        rightRearDriveMotor.setDirection(DcMotor.Direction.REVERSE);

        armExtensionFront.setDirection(DcMotor.Direction.FORWARD);
        armExtensionBack.setDirection(DcMotor.Direction.REVERSE);

        armHeightMotor.setDirection(DcMotor.Direction.REVERSE);

        //airplaneMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        //reset encoders
        /*armHeightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armExtensionBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armExtensionFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);*/

        //set run to position mode
       /* armHeightMotor.setTargetPosition(0);
        armHeightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armExtensionFront.setTargetPosition(0);
        armExtensionFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armExtensionBack.setTargetPosition(0);
        armExtensionBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);*/


        waitForStart();
        runtime.reset();

        //imu parameters
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample OpMode
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);

        //status telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        int i = 0;
        int step = 0;

        while (opModeIsActive()) {
            //input movement values into vector translation in 2d theorem
            double yaw = 0;
            double movementX = 0;
            double movementY = 0;

            /*if (step == 1){
                i+=1;
                if (i > 1000) {
                    yaw = 1;
                    movementY = 1;
                    movementX = 0;
                    armHeightMotor.setPower(1);
                }else{
                    step = 2;
                    i = 0;
                }if(step == 2){
                    i+=1;
                    if (i > 2000){
                        yaw =.5;
                        movementY = .5;
                        movementX = 0;
                    }else{
                        step = 2;
                        i = 0;
                    }
                }
            }else{
                yaw = 0;
                movementX = 0;
                movementY = 0;
            }*/

            //set power variables for Mecanum wheels
            double leftFrontPower = (movementY + movementX + yaw);
            double rightFrontPower = (movementY - movementX - yaw);
            double leftBackPower = (movementY - movementX + yaw);
            double rightBackPower = (movementY + movementX - yaw);

            //normalize power variables to prevent motor power from exceeding 1.0
            double maxPower = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
            maxPower = Math.max(maxPower, Math.abs(leftBackPower));
            maxPower = Math.max(maxPower, Math.abs(rightBackPower));
            if (maxPower > 1.0) {
                leftFrontPower /= maxPower;
                rightFrontPower /= maxPower;
                leftBackPower /= maxPower;
                rightBackPower /= maxPower;

            }

            //apply calculated motor powers
            leftFrontDriveMotor.setPower(leftFrontPower);
            rightFrontDriveMotor.setPower(rightFrontPower);
            leftRearDriveMotor.setPower(leftBackPower);
            rightRearDriveMotor.setPower(rightBackPower);

            //reset field centric button
            if (gamepad1.y) {
                imu.initialize(parameters);
            }

            //joystick height control
            /*double heightDelta = (gamepad2.left_stick_y * HEIGHT_ADJUST_RATE);
            targetHeight += heightDelta;

            double engthDelta = (-gamepad2.right_stick_y * LENGTH_ADJUST_RATE);
            targetLength += lengthDelta;*/

            //min and max of height
           /* targetHeight = max(-1130.0, targetHeight);
            targetHeight = min(targetHeight, maxHeight);
            //min and max of length
            targetLength = max(0.0, targetLength);
            targetLength = min(targetLength, maxLength);*/

            //height preset buttons
            /*if (gamepad2.b){
                targetHeight = scorePresetHeight;
            }
            if (gamepad2.y){
                targetHeight = climbPresetHeight;
            }*/
            if (gamepad2.left_stick_y < 0){
                armHeightMotor.setPower(gamepad2.left_stick_y);
            }
            if (gamepad2.left_stick_y == 0){
                armHeightMotor.setPower(0);
            }
            if (gamepad2.left_stick_y > 0){
                armHeightMotor.setPower(gamepad2.left_stick_y);
            }

            if (gamepad2.right_stick_y < 0){
                armExtensionFront.setPower(-gamepad2.right_stick_y);
                armExtensionBack.setPower(-gamepad2.right_stick_y);
            }
            if (gamepad2.right_stick_y == 0){
                armExtensionFront.setPower(0);
                armExtensionBack.setPower(0);
            }
            if (gamepad2.right_stick_y > 0){
                armExtensionFront.setPower(-gamepad2.right_stick_y);
                armExtensionBack.setPower(-gamepad2.right_stick_y);
            }




            //intake control zone
            //Top Intake Servo variables
            double topIntakeStop = 0.0;
            double topIntake = 1.0;
            double topOuttake = -1.0;

            //Bottom Intake Servo variables
            double bottomIntakeStop = 0.0;
            double bottomIntake = 1.0;
            double bottomOuttake = -1.0;

            //left feed variables
            double leftFeedIntake = -1.0;
            double leftFeedStop = 0.0;
            double leftFeedOuttake = 1.0;

            //right feed variables
            double rightFeedIntake = 1.0;
            double rightFeedStop = 0.0;
            double rightFeedOuttake = -1.0;

            //intake control
            if (gamepad2.left_trigger == 1.0){
                leftFeedServo.setPower(leftFeedIntake);
                rightFeedServo.setPower(rightFeedIntake);
                leftBackFeed.setPower(leftFeedIntake);
                rightBackFeed.setPower(rightFeedIntake);
            }
            if (gamepad2.right_trigger == 1.0){
                leftFeedServo.setPower(leftFeedOuttake);
                rightFeedServo.setPower(rightFeedOuttake);
                leftBackFeed.setPower(leftFeedOuttake);
                rightBackFeed.setPower(rightFeedOuttake);
            }
            if (gamepad2.right_trigger == 0.0 && gamepad2.left_trigger == 0.0){
                leftFeedServo.setPower(leftFeedStop);
                rightFeedServo.setPower(rightFeedStop);
                leftBackFeed.setPower(leftFeedStop);
                rightBackFeed.setPower(rightFeedStop);
            }

            double airplaneServoOut = 1.0;
            double airplaneServoStop = 0.5;
            double airplanePower = -1.0;
            if (gamepad2.dpad_up){
                airplaneServo.setPosition(airplaneServoOut);
            }
            if (gamepad2.dpad_up == false){
                airplaneServo.setPosition(airplaneServoStop);
            }

            if (gamepad2.a){
                airplaneMotor.setPower(airplanePower * 0.90);
            }
            if (gamepad2.b){
                airplaneMotor.setPower(airplanePower * 0.85);
            }
            if (gamepad2.x){
                airplaneMotor.setPower(airplanePower * 0.80);
            }
            if (gamepad2.y){
                airplaneMotor.setPower(airplanePower * 0.77);
            }
            if (gamepad2.a == false && gamepad2.b == false && gamepad2.x == false && gamepad2.y == false){
                airplaneMotor.setPower(0.0);
            }

            if (gamepad2.b){
                armExtensionFront.setPower(0.0);
                armExtensionBack.setPower(0.0);
            }





            //go to target position for height
           /* armHeightMotor.setTargetPosition((int) targetHeight);
            armHeightMotor.setPower(1.0);
            armExtensionFront.setTargetPosition((int) targetLength);
            armExtensionFront.setPower(1.0);
            armExtensionBack.setTargetPosition((int) targetLength);
            armExtensionBack.setPower(1.0);*/


            //Telemetry zone
            telemetry.addData("arm height value", armHeightMotor.getCurrentPosition());
            telemetry.addData("front extension value: ", armExtensionFront.getCurrentPosition());
            telemetry.addData("back extension value: ", armExtensionBack.getCurrentPosition());
            telemetry.addData("left trigger value: ", gamepad2.left_trigger);
            telemetry.addData("right trigger value: ", gamepad2.right_trigger);
            telemetry.addData("airplane Power: ", airplaneMotor.getPower());
            telemetry.update();
        }
    }
    double inputScaling(double x) {
        double sign = Math.signum(x);
        double magnitude = Math.abs(x);
        if (magnitude < JOYSTICK_DEAD_ZONE) {
            magnitude = 0.0;
        } else {
            magnitude = (magnitude - JOYSTICK_DEAD_ZONE) / (1.0 - JOYSTICK_DEAD_ZONE);
        }
        magnitude = Math.pow(magnitude, 2.0);
        return sign * magnitude;
    }
}

