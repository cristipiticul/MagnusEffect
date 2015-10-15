package ro.licj.magnus.shaders;

//
//import ro.licj.magnus.util.FileUtil;
//
///**
// * ShaderProgram Class. Used to load and use Vertex and Fragment shaders easily.
// *
// * Source:
// * https://github.com/sriharshachilakapati/LWJGL-Tutorial-Series/tree/8f4a3e0f22caaf3f87f96f3cb8f4fdda1f7d29a4
// */
//public enum ShaderProgram
//{
//  BALL_TEXTURE_SHADER("BallTexture"), TRAJECTORY_LINE_SHADER("TrajectoryLine");
//
//  private static final String SHADER_FILES_PATH = "ro/licj/magnus/shaders/";
//
//  private String shaderName;
//
//  // ProgramID
//  private int programID;
//
//  // Vertex Shader ID
//  private int vertexShaderID;
//  // Fragment Shader ID
//  private int fragmentShaderID;
//
//  /**
//   * Create a new ShaderProgram.
//   */
//  private ShaderProgram(String shaderName) {
//    this.shaderName = shaderName;
//  }
//
//  public static void initAll() {
//    for (ShaderProgram program : ShaderProgram.values()) {
//      program.init();
//    }
//  }
//
//  private void init() {
//    programID = glCreateProgram();
//    attachFragmentShader(SHADER_FILES_PATH + shaderName + ".fs");
//    attachVertexShader(SHADER_FILES_PATH + shaderName + ".vs");
//    link();
//  }
//
//  private int attachShader(String name, int type) {
//    // Load the source
//    String shaderSource = FileUtil.readFromFile(name);
//
//    // Create the shader and set the source
//    int shaderID = glCreateShader(type);
//    glShaderSource(shaderID, shaderSource);
//
//    // Compile the shader
//    glCompileShader(shaderID);
//
//    // Check for errors
//    if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE)
//      throw new RuntimeException("Error creating vertex shader\n"
//          + glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH)));
//
//    // Attach the shader
//    glAttachShader(programID, shaderID);
//
//    return shaderID;
//  }
//
//  /**
//   * Attach a Vertex Shader to this program.
//   *
//   * @param name The file name of the vertex shader.
//   */
//  public void attachVertexShader(String name)
//  {
//    vertexShaderID = attachShader(name, GL_VERTEX_SHADER);
//  }
//
//  /**
//   * Attach a Fragment Shader to this program.
//   *
//   * @param name
//   *            The file name of the Fragment Shader.
//   */
//  public void attachFragmentShader(String name)
//  {
//    fragmentShaderID = attachShader(name, GL_FRAGMENT_SHADER);
//  }
//
//  /**
//   * Links this program in order to use.
//   */
//  public void link()
//  {
//    // Link this program
//    glLinkProgram(programID);
//
//    // Check for linking errors
//    if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
//      throw new RuntimeException("Unable to link shader program:");
//  }
//
//  /**
//   * Bind this program to use.
//   */
//  public void bind()
//  {
//    glUseProgram(programID);
//  }
//
//  /**
//   * Unbind the shader program.
//   */
//  public static void unbind()
//  {
//    glUseProgram(0);
//  }
//
//  public static void disposeAll() {
//    for (ShaderProgram program : ShaderProgram.values()) {
//      program.dispose();
//    }
//  }
//
//  /**
//   * Dispose the program and shaders.
//   */
//  private void dispose()
//  {
//    // Unbind the program
//    unbind();
//
//    // Detach the shaders
//    glDetachShader(programID, vertexShaderID);
//    glDetachShader(programID, fragmentShaderID);
//
//    // Delete the shaders
//    glDeleteShader(vertexShaderID);
//    glDeleteShader(fragmentShaderID);
//
//    // Delete the program
//    glDeleteProgram(programID);
//  }
//
//  /**
//   * @return The ID of this program.
//   */
//  public int getID()
//  {
//    return programID;
//  }
//}
