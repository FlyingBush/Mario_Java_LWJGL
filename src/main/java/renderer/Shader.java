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
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // Find the first pattern after #type 'pattern'
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // Find the second pattern after #type 'pattern'
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex"))
            {
                vertexShaderSrc = splitString[1];
            } else if (firstPattern.equals("fragment"))
            {
                fragmentShaderSrc = splitString[1];
            } else
            {
                throw new IOException("Unexpected token '" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex"))
            {
                vertexShaderSrc = splitString[2];
            } else if (secondPattern.equals("fragment"))
            {
                fragmentShaderSrc = splitString[2];
            } else
            {
                throw new IOException("Unexpected token '" + secondPattern + "'");
            }

        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error : Could not open file for shader '" + filepath + "'";
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
