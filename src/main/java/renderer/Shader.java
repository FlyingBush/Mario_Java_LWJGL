package renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader {

    private int shaderProgramID;

    private String vertexShaderSrc;
    private String fragmentShaderSrc;
    private String filepath;

    public Shader(String filepath) {
        this.filepath = filepath;
        String source = null;

        // Open and store the glsl file in 'String source'
        try {
            source = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error could not open file for shader : '" + filepath +"'";
        }

        // Split the two different shaders sources
        String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
        // We get an array of 3 : ["", 'the vertex / fragment shader source', 'the vertex / fragment shader source']

        assert splitString.length >= 2 : "Error shader '" + filepath + "' is not a valid shader";

        // Parse the 2 shaders
        String[] shaderType = new String[splitString.length-1];
        int startPos;
        int endPos = 0;
        for (int count = 1; count < 3; count++) {
            startPos = source.indexOf("#type", endPos) + 6;
            endPos = source.indexOf("\r\n", startPos);
            shaderType[count-1] = source.substring(startPos, endPos).trim();

            switch (shaderType[count-1]) {
                case "vertex":
                    vertexShaderSrc = splitString[count];
                    break;
                case "fragment":
                    fragmentShaderSrc = splitString[count];
                    break;
                default:
                    assert false : "Error shader '" + filepath + "' has invalid types";
            }
            ++count;
        }
    }

    public void compileAndLink() {
        // ====================================
        // Compile and link shaders
        // ====================================

        int vertexID, fragmentID;
        // First load and compile the vertex shader

        // Create a shader object
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Next we attach the shader source code to the shader object
        glShaderSource(vertexID, vertexShaderSrc);
        // Compile the shader
        glCompileShader(vertexID);

        // Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\t" +
                    "Vertex shader compilation failed.");
            System.err.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // Create a shader object
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Next we attach the shader source code to the shader object
        glShaderSource(fragmentID, fragmentShaderSrc);
        // Compile the shader
        glCompileShader(fragmentID);

        // Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\t" +
                    "Fragment shader compilation failed.");
            System.err.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Link shaders and check for errors
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Check for linking errors
        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE)
        {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.err.println("ERROR: '" + filepath + "'\n\t" +
                    "Shaders linking failed.");
            System.err.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }

        // Then delete the unuseful shader objects
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    public void use() {
        // Bind the shader program
        glUseProgram(shaderProgramID);
    }

    public void detach() {
        glUseProgram(0);
    }
}
