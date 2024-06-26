// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.SparkAbsoluteEncoder.Type;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkPIDController;

import edu.wpi.first.math.MathUtil;

import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkBase.IdleMode;

import frc.robot.Constants;
import frc.robot.RobotMap;
import frc.robot.testingdashboard.SubsystemBase;
import frc.robot.testingdashboard.TDNumber;

public class BarrelPivot extends SubsystemBase {
  private static BarrelPivot m_barrelPivot;

  TDNumber m_targetAngle;
  TDNumber m_TDangleP;
  TDNumber m_TDangleI;
  TDNumber m_TDangleD;
  double   m_angleP = Constants.kBarrelPivotP;
  double   m_angleI = Constants.kBarrelPivotI;
  double   m_angleD = Constants.kBarrelPivotD;
  private double   m_lastAngle = 0;
  TDNumber m_encoderValueRotations;
  TDNumber m_encoderValueAngleDegrees;
  
  CANSparkMax m_BPLeftSparkMax;
  CANSparkMax m_BPRightSparkMax;
  SparkPIDController m_SparkPIDController;
  AbsoluteEncoder m_absoluteEncoder;

  TDNumber m_leftCurrentOutput;
  TDNumber m_rightCurrentOutput;

  /** Creates a new BarrelPivot. */
  private BarrelPivot() {
    super("BarrelPivot");

    if (RobotMap.BP_ENABLED) {
      m_BPLeftSparkMax = new CANSparkMax(RobotMap.BP_MOTOR_LEFT, MotorType.kBrushless);
      m_BPRightSparkMax = new CANSparkMax(RobotMap.BP_MOTOR_RIGHT, MotorType.kBrushless);

      m_BPLeftSparkMax.restoreFactoryDefaults();
      m_BPRightSparkMax.restoreFactoryDefaults();

      m_BPLeftSparkMax.setIdleMode(IdleMode.kBrake);
      m_BPRightSparkMax.setIdleMode(IdleMode.kBrake);

      m_BPLeftSparkMax.setInverted(true);
      // Motors are set opposite of each other, but they want to spin in the same direction
      m_BPRightSparkMax.follow(m_BPLeftSparkMax, true);

      m_SparkPIDController = m_BPLeftSparkMax.getPIDController();

      m_TDangleP = new TDNumber(this, "Barrel Pivot PID", "P", Constants.kBarrelPivotP);
      m_TDangleI = new TDNumber(this, "Barrel Pivot PID", "I", Constants.kBarrelPivotI);
      m_TDangleD = new TDNumber(this, "Barrel Pivot PID", "D", Constants.kBarrelPivotD);

      m_SparkPIDController.setP(m_angleP);
      m_SparkPIDController.setI(m_angleI);
      m_SparkPIDController.setD(m_angleD);

      m_absoluteEncoder = m_BPLeftSparkMax.getAbsoluteEncoder(Type.kDutyCycle);
      m_SparkPIDController.setFeedbackDevice(m_absoluteEncoder);

      m_absoluteEncoder.setInverted(false);
      m_absoluteEncoder.setPositionConversionFactor(Constants.kBPEncoderPositionFactorDegrees);
      m_targetAngle = new TDNumber(this, "Encoder Values", "Target Angle", getAngle());

      m_SparkPIDController.setPositionPIDWrappingEnabled(true);
      m_SparkPIDController.setPositionPIDWrappingMinInput(0);
      m_SparkPIDController.setPositionPIDWrappingMaxInput(Constants.DEGREES_PER_REVOLUTION);

      m_encoderValueRotations = new TDNumber(this, "Encoder Values", "Rotations", getAngle() / Constants.kBPEncoderPositionFactorDegrees);
      m_encoderValueAngleDegrees = new TDNumber(this, "Encoder Values", "Angle (degrees)", getAngle());

      m_leftCurrentOutput = new TDNumber(Drive.getInstance(), "Current", "Barrel Pivot Left Output", m_BPLeftSparkMax.getOutputCurrent());
      m_rightCurrentOutput = new TDNumber(Drive.getInstance(), "Current", "Barrel Pivot Right Output", m_BPRightSparkMax.getOutputCurrent());

      m_BPLeftSparkMax.burnFlash();
      m_BPRightSparkMax.burnFlash();
    }
  }

  public static BarrelPivot getInstance() {
    if (m_barrelPivot == null) {
      m_barrelPivot = new BarrelPivot();
    }
    return m_barrelPivot;
  }

  public double getAngle() {
    return m_absoluteEncoder.getPosition();
  }

  public void setTargetAngle(double angle) {
    double setPoint = angle % Constants.DEGREES_PER_REVOLUTION;
    setPoint  = MathUtil.clamp(setPoint,
                               Constants.kBarrelPivotLowerLimitDegrees, 
                               Constants.kBarrelPivotUpperLimitDegrees);
    if (setPoint != m_lastAngle)
    {
      m_targetAngle.set(setPoint);
      m_lastAngle = setPoint;
      m_SparkPIDController.setReference(setPoint, ControlType.kPosition);
    }
  }

  public void resetTargetAngle() {
    setTargetAngle(getAngle());
  }

  public double getTargetAngle() {
    return m_targetAngle.get();
  }

  public void setZeroAsCurrentPosition() {
    m_absoluteEncoder.setZeroOffset(getAngle());
    resetTargetAngle(); 
  }

  public boolean alignedToSource() {
    return MathUtil.isNear(Constants.BP_SOURCE_ANGLE_DEGREES, m_barrelPivot.getAngle(), Constants.BP_ANGLE_TOLERANCE_DEGREES);
  }

  public boolean alignedToAmp() {
    return MathUtil.isNear(Constants.BP_AMP_SCORING_ANGLE_DEGREES, m_barrelPivot.getAngle(), Constants.BP_ANGLE_TOLERANCE_DEGREES);
  }

  public boolean alignedToShootClose() {
    return MathUtil.isNear(Constants.BP_SHOOTER_SCORING_ANGLE_DEGREES, m_barrelPivot.getAngle(), Constants.BP_ANGLE_TOLERANCE_DEGREES);
  }

  public boolean atGoal() {
    return MathUtil.isNear(getTargetAngle(), getAngle(), Constants.BP_SPEAKER_TOLERANCE_DEGREES);
  }

  public void pivotUpwards() {
    if (m_BPLeftSparkMax != null) {
      m_BPLeftSparkMax.set(Constants.BP_SPEED);
    }
  }

  public void pivotDownwards() {
    if (m_BPLeftSparkMax != null) {
      m_BPLeftSparkMax.set(-Constants.BP_SPEED);
    }
  }

  public void stopPivot() {
    if (m_BPLeftSparkMax != null) {
      m_BPLeftSparkMax.set(0);
    }
  }

  @Override
  public void periodic() {
    if (RobotMap.BP_ENABLED) {
      if (Constants.kEnableBarrelPivotPIDTuning) {
        double tmp = m_TDangleP.get();
        if (tmp != m_angleP) {
          m_SparkPIDController.setP(tmp);
          m_angleP = tmp;
        }
        tmp = m_TDangleI.get();
        if (tmp != m_angleI) {
          m_SparkPIDController.setI(tmp);
          m_angleI = tmp;
        }
        tmp = m_TDangleD.get();
        if (tmp != m_angleD) {
          m_SparkPIDController.setD(tmp);
          m_angleD = tmp;
        }
      }

      m_encoderValueRotations.set(getAngle() / Constants.kBPEncoderPositionFactorDegrees);
      m_encoderValueAngleDegrees.set(getAngle());

      m_leftCurrentOutput.set(m_BPLeftSparkMax.getOutputCurrent());
      m_rightCurrentOutput.set(m_BPRightSparkMax.getOutputCurrent());
    }

    super.periodic();
  }
}
