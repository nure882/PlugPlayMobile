namespace PlugPlay.Domain.Entities;

public class ProductAttribute
{
    public int Id { get; set; }

    public int AttributeId { get; set; }

    public int ProductId { get; set; }

    public string Value { get; set; }
    
    public Product Product { get; set; }

    public Attribute Attribute { get; set; }

    public (dynamic, Type) GetTypedValue()
    {
        if (Attribute == null)
        {
            return (null, null);
        }

        var type = GetValueType();

        if (type == null)
        {
            return (null, null);
        }

        if (type == typeof(int))
        {
            if (int.TryParse(Value, out var i))
            {
                return (i, type);
            }

            if (double.TryParse(Value, out var d))
            {
                return ((int)d, type);
            }

            return (null, type);
        }

        if (type == typeof(bool))
        {
            if (bool.TryParse(Value, out var b))
                return (b, type);

            if (Value == "0")
            {
                return (false, type);
            }

            if (Value == "1")
            {
                return (true, type);
            }

            return (null, type);
        }

        if (type == typeof(string))
        {
            return (Value, type);
        }

        try
        {
            var converted = Convert.ChangeType(Value, type);

            return (converted, type);
        }
        catch
        {
            return (null, type);
        }
    }

    private Type GetValueType()
    {
        var dataType = "";
        dataType = Attribute?.DataType;

        switch (dataType)
        {
            case "decimal":
                return typeof(int);
            case "string":
                return typeof(string);
            case "bool":
                return typeof(bool);
            case "":
                break;
        }

        return null;
    }
}
