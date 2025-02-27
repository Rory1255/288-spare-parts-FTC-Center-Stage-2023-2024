package org.firstinspires.ftc.teamcode.drive;


import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import android.annotation.SuppressLint;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import org.firstinspires.ftc.teamcode.drive.WebcamAprilTagTesting;
@Disabled
@TeleOp(name = "Wheels Go Brrr", group = "Linear OpMode")
public class driveTestForReal extends LinearOpMode {
    //motor variable declarations
    private final ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftFrontDriveMotor = null;
    private DcMotor rightFrontDriveMotor = null;
    private DcMotor leftRearDriveMotor = null;
    private DcMotor rightRearDriveMotor = null;
    //joystick variables
    final double JOYSTICK_DEAD_ZONE = 0.20;
    final double JOYSTICK_MOVEMENT_SENSITIVITY = 0.75;
    final double JOYSTICK_ROTATION_SENSITIVITY = 1.00;
    //servos
    private CRServo topIntakeServo = null;
    private CRServo bottomIntakeServo = null;
    private Servo leftFeedServo = null;
    private Servo rightFeedServo = null;
    private Servo hookServo = null;
    //imu
    private BNO055IMU imu;

    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode(){

        //Hardware mapping
       leftFrontDriveMotor = hardwareMap.get(DcMotor.class, "leftFrontDriveMotor");
       rightFrontDriveMotor = hardwareMap.get(DcMotor.class, "rightFrontDriveMotor");
       leftRearDriveMotor = hardwareMap.get(DcMotor.class, "Leftreardrivemotor");
       rightRearDriveMotor = hardwareMap.get(DcMotor.class, "Rightreardrivemotor");

       topIntakeServo = hardwareMap.get(CRServo.class, "topIntakeServo");
       bottomIntakeServo = hardwareMap.get(CRServo.class, "bottomIntakeServo");
       leftFeedServo = hardwareMap.get(Servo.class, "leftFeedServo");
       rightFeedServo = hardwareMap.get(Servo.class, "rightFeedServo");

      // hookServo = hardwareMap.get(Servo.class, "hookServo");

       //Setting zero power behavior
       leftRearDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
       rightRearDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
       leftFrontDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
       rightFrontDriveMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

       //Setting motor direction after empirical testing
        leftFrontDriveMotor.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDriveMotor.setDirection(DcMotor.Direction.REVERSE);
        leftRearDriveMotor.setDirection(DcMotor.Direction.FORWARD);
        rightRearDriveMotor.setDirection(DcMotor.Direction.REVERSE);

        //Run without internal encoders
        leftFrontDriveMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //wait for start button to be pressed
        waitForStart();
        runtime.reset();

        //set imu parameters
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample OpMode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
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

        //Loop until stop button is pressed
        while (opModeIsActive()){

            //scale joystick values and apply sensitivity
            double joystickMovementY = inputScaling(-gamepad1.left_stick_y) * JOYSTICK_MOVEMENT_SENSITIVITY;  // Note: pushing stick forward gives negative value
            double joystickMovementX = inputScaling(gamepad1.left_stick_x) * JOYSTICK_MOVEMENT_SENSITIVITY;
            double yaw = (inputScaling(gamepad1.right_stick_x) * JOYSTICK_ROTATION_SENSITIVITY) * 0.75;

            //get robot orientation from imu
            double robotHeading = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;

            //input movement values into vector translation in 2d theorem
            double theta = -robotHeading;
            double movementX = joystickMovementX * cos(toRadians(theta)) - joystickMovementY * sin(toRadians(theta));
            double movementY = joystickMovementX * sin(toRadians(theta)) + joystickMovementY * cos(toRadians(theta));

            //logic to reduce speed of robot when left trigger is pressed and return to full speed when released
            if (gamepad1.left_trigger > 0.000) {
                movementX = movementX * 0.45;
                movementY = movementY * 0.45;
                yaw = yaw * 0.45;
            }
            if (gamepad1.left_trigger > 0.000 && gamepad1.left_trigger < 0.001) {
                movementX = movementX / 0.45;
                movementY = movementY / 0.45;
                yaw = yaw / 0.45;
            }

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
            if (gamepad1.b){
                imu.initialize(parameters);
            }

            //Top Intake Servo variables
            double topIntakeStop = 0.0;
            double topIntake = 1.0;
            double topOuttake = -1.0;

            //Bottom Intake Servo variables
            double bottomIntakeStop = 0.0;
            double bottomIntake = 1.0;
            double bottomOuttake = -1.0;

            //left feed variables
            double leftFeedIntake = 0.0;
            double leftFeedStop = 0.5;
            double leftFeedOuttake = 1.0;

            //right feed variables
            double rightFeedIntake = 1.0;
            double rightFeedStop = 0.5;
            double rightFeedOuttake = 0.0;

            //intake control
            if (gamepad2.left_stick_y < 0){
                topIntakeServo.setPower(topIntake);
                bottomIntakeServo.setPower(bottomIntake);
            }
            if (gamepad2.left_stick_y == 0){
                topIntakeServo.setPower(topIntakeStop);
                bottomIntakeServo.setPower(bottomIntakeStop);
            }
            if (gamepad2.left_stick_y > 0){
                topIntakeServo.setPower(topOuttake);
                bottomIntakeServo.setPower(bottomOuttake);
            }

            //feed control
            //feed in
            if (gamepad2.a){
                leftFeedServo.setPosition(leftFeedIntake);
                rightFeedServo.setPosition(rightFeedIntake);
            }
            //feed out
            if (gamepad2.x){
                leftFeedServo.setPosition(leftFeedOuttake);
                rightFeedServo.setPosition(rightFeedOuttake);
            }
            //feed stop
            if (gamepad2.y){
                leftFeedServo.setPosition(leftFeedStop);
                rightFeedServo.setPosition(rightFeedStop);
            }





            telemetry.update();
        }
    }

    //input scaling algorithm
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

