// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Lights;

import frc.robot.testingdashboard.Command;
import frc.robot.subsystems.Lights;

public class EnableLights extends Command {
  Lights m_lights;

  /** Creates a new EnableLights. */
  public EnableLights() {
    super(Lights.getInstance(), "Basic", "EnableLights");
    m_lights = Lights.getInstance();
    addRequirements(m_lights);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_lights.enableLights();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return true;
  }
}
