package de.fhws.fiw.pvs.exam.resources;

public class UserData
{
    String cn;
    String role;

    public UserData()
    {
    }

    public UserData(String cn, String role)
    {
        this.cn = cn;
        this.role = role;
    }

    public String getCn()
    {
        return cn;
    }

    public void setCn(String cn)
    {
        this.cn = cn;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }
}
