package green.zerolabs.gqgen;

import graphql.language.*;
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import green.zerolabs.gqgen.model.Constants;
import io.smallrye.graphql.client.generator.GraphQLGeneratorException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Generator {
  private final String pkg;
  private final String apiTypeName;
  private final String schemaString;
  private final List<String> queryStrings;

  private TypeDefinitionRegistry schema;
  private List<Document> queries;

  public Generator(
      final String pkg, final String apiTypeName, final String schema, final List<String> queries) {
    this.pkg = pkg;
    this.apiTypeName = apiTypeName;
    schemaString = schema;
    queryStrings = queries;
  }

  public Map<String, String> generateSourceFiles() {
    schema = parseSchema();
    queries = parseQueries();
    final Map<String, String> sourceFiles = new LinkedHashMap<>();

    new Api().addTo(sourceFiles);

    return sourceFiles;
  }

  private TypeDefinitionRegistry parseSchema() {
    try {
      return new SchemaParser().parse(schemaString);
    } catch (final Exception e) {
      throw new GraphQLGeneratorException("can't parse schema: " + schemaString, e);
    }
  }

  private List<Document> parseQueries() {
    return queryStrings.stream().map(this::query).collect(toList());
  }

  private Document query(final String query) {
    try {
      return Parser.parse(query);
    } catch (final Exception e) {
      throw new GraphQLGeneratorException("can't parse query: " + query, e);
    }
  }

  private abstract class SourceFileGenerator {
    protected String typeType;

    protected final Set<String> imports = new TreeSet<>();

    protected abstract String generateBody();

    final List<SourceFileGenerator> other = new ArrayList<>();

    public SourceFileGenerator(final String typeType) {
      this.typeType = typeType;
    }

    public abstract String getTypeName();

    protected String getImplements() {
      return StringUtils.EMPTY;
    }

    protected class JavaType {
      private final Type<?> type;
      private final List<String> fieldNames;

      public JavaType(final Type<?> type, final List<String> fieldNames) {
        this.type = type;
        this.fieldNames = fieldNames;
      }

      @Override
      public String toString() {
        if (type instanceof ListType) {
          return toListJava((ListType) type);
        }
        if (type instanceof NonNullType) {
          return toNonNullJava((NonNullType) type);
        }
        if (type instanceof TypeName) {
          return toJava((TypeName) type);
        }
        throw new UnsupportedOperationException("unexpected type of type"); // unreachable
      }

      private String toListJava(final ListType type) {
        imports.add("java.util.List");
        return "List<" + new JavaType(type.getType(), fieldNames) + ">";
      }

      private String toNonNullJava(final NonNullType type) {
        imports.add("org.eclipse.microprofile.graphql.NonNull");
        return "@NonNull " + new JavaType(type.getType(), fieldNames);
      }

      private String toJava(final TypeName typeName) {
        final String string = typeName.getName();
        switch (string) {
          case "Int":
            return "Long";
          case "Float":
            return "java.math.BigDecimal";
          case "Boolean":
          case "String":
            return string;
          case "ID":
          case "AWSDate":
          case "AWSTime":
          case "AWSDateTime":
          case "AWSTimestamp":
          case "AWSEmail":
          case "AWSPhone":
          case "AWSURL":
          case "AWSIPAddress":
            return "String";
          case "AWSJSON":
            return "Object";
          default:
            other.stream()
                .filter(generator -> generator instanceof TypeGenerator)
                .filter(generator -> generator.getTypeName().equals(string))
                .findFirst()
                .ifPresentOrElse(
                    sourceFileGenerator -> {},
                    () -> other.add(new TypeGenerator(string, fieldNames, List.of())));
            return string;
        }
      }
    }

    public void addTo(final Map<String, String> sourceFiles) {
      final String body = generateBody();
      final boolean isClass = StringUtils.equals("class", typeType);
      final boolean isEnum = StringUtils.equals("enum", typeType);
      final boolean isInterface = StringUtils.equals("interface", typeType);
      final String source =
          "package "
              + pkg
              + ";\n"
              + "\n"
              + imports()
              + "// Autogenerated. Do not modify. Will be overridden. Generated by "
              + getClass().getName()
              + "\n"
              + (isClass || isEnum ? "@io.quarkus.runtime.annotations.RegisterForReflection\n" : "")
              + (isClass ? "@lombok.Data\n" : "")
              + (isClass ? "@lombok.NoArgsConstructor\n" : "")
              + "public "
              + typeType
              + " "
              + getTypeName()
              + getImplements()
              + (isInterface ? " extends java.io.Serializable " : "")
              + " {\n"
              + body
              + "}\n";
      final String key = pkg + "." + getTypeName();
      final String previousSource = sourceFiles.put(key, source);
      if (previousSource != null && !previousSource.equals(source)) {
        if (source.length() < previousSource.length()) {
          sourceFiles.put(key, previousSource);
        }
      }
      other.forEach(it -> it.addTo(sourceFiles));
    }

    private String imports() {
      return imports.isEmpty()
          ? ""
          : imports.stream().collect(joining(";\nimport ", "import ", ";\n\n"));
    }
  }

  private class Api extends SourceFileGenerator {
    private final StringBuilder body = new StringBuilder();

    public Api() {
      super("interface");
    }

    @java.lang.Override
    public String getTypeName() {
      return apiTypeName;
    }

    @java.lang.Override
    protected String generateBody() {
      queries.forEach(this::generateQueryMethod);

      return body.toString();
    }

    @java.lang.Override
    public void addTo(final Map<String, String> sourceFiles) {
      schema.types().entrySet().stream()
          .filter(
              entry -> !StringUtils.equalsAny(entry.getKey(), Constants.MUTATION, Constants.QUERY))
          .forEach(
              entry -> {
                final TypeDefinition typeDefinition = entry.getValue();
                if (typeDefinition instanceof InputObjectTypeDefinition) {
                  final InputObjectTypeDefinition typeDefinition1 =
                      (InputObjectTypeDefinition) typeDefinition;
                  other.add(
                      new TypeGenerator(
                          typeDefinition1.getName(),
                          typeDefinition1.getInputValueDefinitions().stream()
                              .map(definition -> definition.getName())
                              .collect(toList()),
                          List.of()));
                }
                if (typeDefinition instanceof InterfaceTypeDefinition) {
                  final InterfaceTypeDefinition typeDefinition1 =
                      (InterfaceTypeDefinition) typeDefinition;
                  other.add(
                      new TypeGenerator(
                          typeDefinition1.getName(),
                          typeDefinition1.getFieldDefinitions().stream()
                              .map(definition -> definition.getName())
                              .collect(toList()),
                          List.of()));
                }
                if (typeDefinition instanceof ObjectTypeDefinition) {
                  final ObjectTypeDefinition typeDefinition1 =
                      (ObjectTypeDefinition) typeDefinition;
                  other.add(
                      new TypeGenerator(
                          typeDefinition1.getName(),
                          typeDefinition1.getFieldDefinitions().stream()
                              .map(definition -> definition.getName())
                              .collect(toList()),
                          Optional.ofNullable(typeDefinition1.getImplements()).stream()
                              .flatMap(Collection::stream)
                              .filter(type -> type instanceof TypeName)
                              .map(type -> ((TypeName) type).getName())
                              .collect(toList())));
                }
              });
      super.addTo(sourceFiles);
    }

    private void generateQueryMethod(final Document query) {
      final List<OperationDefinition> definitions =
          query.getDefinitionsOfType(OperationDefinition.class);
      if (definitions.size() != 1) {
        throw new GraphQLGeneratorException(
            "expected exactly one definition but found "
                + definitions.stream().map(this::operationInfo).collect(listString()));
      }
      final OperationDefinition operation = definitions.get(0);
      final List<Field> fields = operation.getSelectionSet().getSelectionsOfType(Field.class);
      if (fields.size() != 1) {
        throw new GraphQLGeneratorException(
            "expected exactly one field but got "
                + fields.stream().map(Field::getName).collect(listString()));
      }
      final Field field = fields.get(0);
      body.append(new MethodGenerator(operation, field));
    }

    private String operationInfo(final OperationDefinition definition) {
      return definition.getOperation().toString().toLowerCase() + " " + definition.getName();
    }

    private class MethodGenerator {
      private final OperationDefinition operation;
      private final Field method;

      private final StringBuilder annotations = new StringBuilder();

      public MethodGenerator(final OperationDefinition operation, final Field method) {
        this.operation = operation;
        this.method = method;
      }

      @Override
      public String toString() {
        final JavaType returnType = returnType();
        final String methodName = methodName();
        final String argumentList = argumentList();
        return "    " + annotations + returnType + " " + methodName + argumentList + ";\n";
      }

      private JavaType returnType() {
        final ObjectTypeDefinition query =
            (ObjectTypeDefinition)
                schema
                    .getType("Query")
                    .orElseThrow(
                        () -> new GraphQLGeneratorException("'Query' type not found in schema"));
        final Stream<ObjectTypeDefinition> objects =
            Stream.of(query, (ObjectTypeDefinition) schema.getType("Mutation").orElse(null))
                .filter(objectTypeDefinition -> objectTypeDefinition != null);

        final FieldDefinition fieldDefinition =
            objects
                .flatMap(
                    objectTypeDefinition -> objectTypeDefinition.getFieldDefinitions().stream())
                .filter(f -> f.getName().equals(method.getName()))
                .findAny()
                .orElseThrow(
                    () ->
                        new GraphQLGeneratorException(
                            "field (method) '"
                                + method.getName()
                                + "' not found in "
                                + query.getFieldDefinitions().stream()
                                    .map(FieldDefinition::getName)
                                    .collect(listString())));
        final List<String> selections =
            operation.getSelectionSet().getSelectionsOfType(Field.class).stream()
                .flatMap(this::selectedFields)
                .map(Field::getName)
                .collect(toList());
        return new JavaType(fieldDefinition.getType(), selections);
      }

      private Stream<? extends Field> selectedFields(final Field field) {
        final SelectionSet selectionSet = field.getSelectionSet();
        return (selectionSet == null)
            ? Stream.of()
            : selectionSet.getSelectionsOfType(Field.class).stream();
      }

      public String methodName() {
        if (method.getAlias() != null) {
          nameQuery(operation);
          return method.getAlias();
        }
        if (operation.getName() == null) {
          return method.getName();
        }
        if (!operation.getName().equals(method.getName())) {
          nameQuery(operation);
        }
        return operation.getName();
      }

      private void nameQuery(final OperationDefinition operation) {
        final OperationDefinition.Operation operationEnum =
            Optional.ofNullable(operation).map(OperationDefinition::getOperation).orElse(null);
        final boolean isMutation = OperationDefinition.Operation.MUTATION.equals(operationEnum);

        final Class annotationClass = isMutation ? Mutation.class : Query.class;
        imports.add(annotationClass.getName());
        annotations
            .append("@" + annotationClass.getSimpleName() + "(\"")
            .append(method.getName())
            .append("\") ");
      }

      public String argumentList() {
        return new ArgumentList(method, operation.getVariableDefinitions()).toString();
      }

      private class ArgumentList {
        private final Field field;
        private final List<VariableDefinition> variableDefinitions;

        public ArgumentList(final Field field, final List<VariableDefinition> variableDefinitions) {
          this.field = field;
          this.variableDefinitions = variableDefinitions;
        }

        @Override
        public String toString() {
          return field.getArguments().stream()
              .map(
                  argument -> {
                    final List<String> fields = getFields(argument);
                    return new JavaType(type(argument), fields) + " " + argument.getName();
                  })
              .collect(joining(", ", "(", ")"));
        }

        private List<String> getFields(final Argument argument) {
          final Value<?> value = argument.getValue();
          if (!(value instanceof VariableReference)) {
            return emptyList();
          }

          final VariableReference variableReference = (VariableReference) value;
          return variableDefinitions.stream()
              .filter(
                  variableDefinition ->
                      variableDefinition.getName().equals(variableReference.getName()))
              .map(
                  variableDefinition -> {
                    final Type type = variableDefinition.getType();
                    if (type instanceof NonNullType) {
                      final NonNullType type1 = (NonNullType) type;
                      final Type type2 = type1.getType();
                      if (type2 instanceof TypeName) {
                        return List.<String>of(((TypeName) type2).getName());
                      }
                    }
                    if (type instanceof TypeName) {
                      return List.<String>of(((TypeName) type).getName());
                    }
                    return List.<String>of();
                  })
              .findFirst()
              .orElse(List.<String>of());
        }

        private Stream<? extends Field> selectedFields(final Field field) {
          final SelectionSet selectionSet = field.getSelectionSet();
          return (selectionSet == null)
              ? Stream.of()
              : selectionSet.getSelectionsOfType(Field.class).stream();
        }

        private Type<?> type(final Argument argument) {

          final Value<?> value = argument.getValue();
          if (value instanceof VariableReference) {
            return resolve(((VariableReference) value).getName());
          }
          throw new GraphQLGeneratorException(
              "unsupported type " + value + " for argument '" + argument.getName() + "'");
        }

        private Type<?> resolve(final String name) {
          return variableDefinitions.stream()
              .filter(var -> var.getName().equals(name))
              .findAny()
              .map(VariableDefinition::getType)
              .orElseThrow(
                  () ->
                      new GraphQLGeneratorException(
                          "no definition found for parameter '"
                              + name
                              + "' in "
                              + variableDefinitions.stream()
                                  .map(VariableDefinition::getName)
                                  .collect(listString())));
        }
      }
    }
  }

  private class TypeGenerator extends SourceFileGenerator {
    private final String typeName;
    private final List<String> fieldNames;
    private final List<String> implementations;

    public TypeGenerator(
        final String typeName, final List<String> fieldNames, final List<String> implementations) {
      super("class");
      this.typeName = typeName;
      this.fieldNames = fieldNames;
      this.implementations = implementations;
    }

    @java.lang.Override
    public String getTypeName() {
      return typeName;
    }

    @java.lang.Override
    protected String getImplements() {
      final boolean isClass = StringUtils.equals("class", typeType);

      final Stream<String> serializable =
          isClass ? Stream.of("java.io.Serializable") : Stream.empty();
      final Stream<String> implementationsLocal =
          CollectionUtils.isEmpty(implementations)
              ? serializable
              : Stream.concat(serializable, implementations.stream());
      return implementationsLocal
          .reduce((s, s2) -> s + "," + s2)
          .map(s -> " implements " + s + StringUtils.SPACE)
          .orElse(StringUtils.EMPTY);
    }

    @java.lang.Override
    protected String generateBody() {
      final Optional<TypeDefinition> typeOpt = schema.getType(typeName);

      final TypeDefinition type =
          typeOpt.orElseThrow(
              () -> new GraphQLGeneratorException("type '" + typeName + "' not found in schema"));

      if (type instanceof EnumTypeDefinition) {
        final EnumTypeDefinition typeDefinition = (EnumTypeDefinition) type;

        typeType = "enum";

        return typeDefinition.getEnumValueDefinitions().stream()
            .map(definition -> definition.getName())
            .reduce((s, s2) -> s + "," + s2)
            .orElse("");
      }

      if (type instanceof InputObjectTypeDefinition) {
        final InputObjectTypeDefinition typeDefinition = (InputObjectTypeDefinition) type;

        return typeDefinition.getInputValueDefinitions().stream()
            // .filter(field -> fieldNames.contains(field.getName()))
            .map(this::toJava)
            .collect(joining(";\n    ", "    ", ";\n"));
      }

      if (type instanceof InterfaceTypeDefinition) {
        final InterfaceTypeDefinition typeDefinition = (InterfaceTypeDefinition) type;

        typeType = "interface";

        return typeDefinition.getFieldDefinitions().stream()
            // .filter(field -> fieldNames.contains(field.getName()))
            .flatMap(this::toJavaGetterSetter)
            .collect(joining(";\n    ", "    ", ";\n"));
      }

      final ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition) type;
      return typeDefinition.getFieldDefinitions().stream()
          .filter(field -> fieldNames.contains(field.getName()))
          .map(this::toJava)
          .collect(joining(";\n    ", "    ", ";\n"));
    }

    private Stream<String> toJavaGetterSetter(final FieldDefinition field) {
      final String fieldName = StringUtils.capitalize(field.getName());
      final JavaType javaType = new JavaType(field.getType(), fieldNames);
      return Stream.of(
          javaType + " get" + fieldName + "()",
          "void set" + fieldName + "(" + javaType + " " + field.getName() + ")");
    }

    private String toJava(final FieldDefinition field) {
      return new JavaType(field.getType(), fieldNames) + " " + field.getName();
    }

    private String toJava(final InputValueDefinition field) {
      return new JavaType(field.getType(), fieldNames) + " " + field.getName();
    }
  }

  private static Collector<CharSequence, ?, String> listString() {
    return joining(", ", "[", "]");
  }
}
