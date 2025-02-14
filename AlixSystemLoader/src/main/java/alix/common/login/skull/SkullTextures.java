package alix.common.login.skull;

public final class SkullTextures {

    public static String encodeSkullProperty(byte digit, SkullTextureType skullType) {
        switch (skullType) {
            case WOODEN_SKULL:
                return encoded_WoodenDigitSkullProperty(digit);
            case CHAT_STONE_SKULL:
                return encoded_ChatStoneSkullProperty(digit);
            case PLUSH_SKULL:
                return encoded_PlushSkullProperty(digit);
            case QUARTZ_SKULL:
                return encoded_QuartzSkullProperty(digit);
        }
        throw new AssertionError("Invalid: " + skullType);
    }

    private static String encoded_QuartzSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY4ODZkOWM0MGVmN2Y1MGMyMzg4MjQ3OTJjNDFmYmZiNTRmNjY1ZjE1OWJmMWJjYjBiMjdiM2VhZDM3M2IifX19";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBhMTllMjNkMjFmMmRiMDYzY2M1NWU5OWFlODc0ZGM4YjIzYmU3NzliZTM0ZTUyZTdjN2I5YTI1In19fQ==";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M1OTZhNDFkYWVhNTFiZTJlOWZlYzdkZTJkODkwNjhlMmZhNjFjOWQ1N2E4YmRkZTQ0YjU1OTM3YjYwMzcifX19";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg1ZDRmZGE1NmJmZWI4NTEyNDQ2MGZmNzJiMjUxZGNhOGQxZGViNjU3ODA3MGQ2MTJiMmQzYWRiZjVhOCJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg1MmEyNWZlNjljYTg2ZmI5ODJmYjNjYzdhYzk3OTNmNzM1NmI1MGI5MmNiMGUxOTNkNmI0NjMyYTliZDYyOSJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlZTdkOTU0ZWIxNGE1Y2NkMzQ2MjY2MjMxYmY5YTY3MTY1MjdiNTliYmNkNzk1NmNlZjA0YTlkOWIifX19";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MmEzYWU5NDgzNzRlMDM3ZTNkN2RkNjg3ZDU5ZDE4NWRkMmNjOGZjMDlkZmViNDJmOThmOGQyNTllNWMzIn19fQ==";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVhMzBjMjRjNjBiM2JjMWFmNjU4ZWY2NjFiNzcxYzQ4ZDViOWM5ZTI4MTg4Y2Y5ZGU5ZjgzMjQyMmU1MTAifX19";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZhYmFmZDAyM2YyMzBlNDQ4NWFhZjI2ZTE5MzY4ZjU5ODBkNGYxNGE1OWZjYzZkMTFhNDQ2Njk5NDg5MiJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ3OTEwZTEwMzM0Zjg5MGE2MjU0ODNhYzBjODI0YjVlNGExYTRiMTVhOTU2MzI3YTNlM2FlNDU4ZDllYTQifX19";
        }
        throw new AssertionError("Invalid: " + digit);
    }


    private static String encoded_PlushSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYzZDE5NzJjZTJmMmI3OWIyZGYwN2FkNWM1NzRmOWNjZDBlY2ViMjA0MTQzZGY5NjZkMzAxYjE4ZTkyYjBiNiJ9fX0=";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBhNGM5NGRkNjVhN2NhZGM0MTcyY2VkZjM5OTBhODU0Mjc3MmJiMTEzY2RmMWEwZjc4ZWY2NTJjNmFiZTZjYiJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ3YzU1MDY0ZjFkMDc2MjVhZjA1Y2RiYzBmYzdjODQxNzNiMTA1NDEwMjRjODg1NWVkNzEzMmNhNzc2NzI0NiJ9fX0=";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDljYjk0ZDg3MjkxOTdhMThhNzY4ODJjMjQ5MThhOGMwNDQ5NGQzNDcyN2Y0ZGYwNjBmYjdhNDVhMWZmODUzNyJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZhZDc1NjVmYTY4OTQyZDJmZjVmMDRlZmRhODBkOWU5Zjg4ZDMzNDAxNjgwZjE4MmUwZjM3YWYzMmI4YzI5NCJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAyMDNiMTI0YWNlZmQwOGM0NDZhMDQ5MmE1YzlkODAwZWUwY2Q4YzI2MGMzYzEyMzc3NzMxNjU5MjUxYWQ0ZSJ9fX0=";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTQ5MTAxN2ZkNTI2N2ZjOWE2NDFjY2UwMjUwZDQ2ODM4MDRlM2IxMzk5YjJiNWI1OGYxMzBmNTc3NGRlMjgyMSJ9fX0=";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyNWFjYzQyY2FmOGIxZTk1Y2NiMTZkMzc5YWYwYjc2Zjk1ZWQyOWVmMmE0OTQwNzNkMGIwN2IxNWRjMjJmZCJ9fX0=";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQ5MDRkNWRlZTM3ODJkNGQ2YzZmZWJlNzI3ODNkOTYxNDgyNzdlZmJlNzhjMTUxNGNjMjAwODhkOGZmMGQwOSJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFkYzY1YzNiODk2YWI2NmRmMWVkN2JhNDI1MGQxNmJiODM1MmE2ZDI4NWE1NGMwMDIxOGVhNzJhNGJiNzE0OSJ9fX0=";
        }
        throw new AssertionError("Invalid: " + digit);
    }

    private static String encoded_ChatStoneSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI4N2I0NDExMWE5ZjMzOWVkNzAxNWQwZjJjYjY0NmNlNmI4YzU5YTBiNzUwYjI3MjQ0OWFlYWMyNTYyNWRmYSJ9fX0=";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJkNGE2OTkzN2UwYmVhZGMzODQyNmMwOTk0YjUwZDk1MDQwNmZkOGRhOWYzMWM1ODJkNDZmM2I5YmZjNGM1YiJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBhNmM3YTBkNjU4YmI5MGUyN2I1OTM0ZjYyYTVlMTVjYzljMTFjODdhZTE0NjRhNGU3OWVhNjY1MjNiYTM2MSJ9fX0=";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYxYjMxYTg3Yjc4MjYyYzYzZTk0NzE0ZTU2MjRhMmFiNTk1MGY3NWRlZTMyY2MzMDI2YTVmYTc4MjM0NjhkZSJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FkZmQzYzk5OTY3ZDMyNzQ5MDJlY2I2ZTk4NjU4YWNmZGIzOTE4NzE3YjJlOTAzN2Y2MWMzYjRlMDllMmExIn19fQ==";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJiYWYwMTkwOTIyMWI5YWJlOTQ1YWZlN2RmZGI3MmYzMTczMzExZTU2MjAxOTRkZDI3MDExYTZkNTU0ZmZjOCJ9fX0=";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZhZTBmZTIyNTZhZTM1NmEyNWYxMzBhZTcxY2Y0NDMxNTE1N2M1ZmFlOTFkNjJhNGZmYjU4NWIxNjQ4NjM3MyJ9fX0=";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGYwOWVmZTczMWU3M2M4MGIxYWVlMTAwYmIzMzBhYjQxNDU5NWVlNTRhNGUyZGVjNDM5YmVkM2UzNjQ5YWM5NiJ9fX0=";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcyN2Q0ZTQ4ZjIzMWNlNGQ4NzE5OTI1NjBmNTFiZjZhM2YxNTdjMmZmZDZmOTJiODYwY2JiNTMxMjg0MjZhMiJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ3M2VhMzUxZjQxZTk5NTdmOTE0ZTNiOTBmNzRlOTg2NzgzOGIzMzM5ZDQyNTEzY2EyNWVkMGY0NWJjNjBjYiJ9fX0=";
        }
        throw new AssertionError("Invalid: " + digit);
    }

    private static String encoded_WoodenDigitSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGViZTdlNTIxNTE2OWE2OTlhY2M2Y2VmYTdiNzNmZGIxMDhkYjg3YmI2ZGFlMjg0OWZiZTI0NzE0YjI3In19fQ==";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlNzhmYjIyNDI0MjMyZGMyN2I4MWZiY2I0N2ZkMjRjMWFjZjc2MDk4NzUzZjJkOWMyODU5ODI4N2RiNSJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ1N2UzYmM4OGE2NTczMGUzMWExNGUzZjQxZTAzOGE1ZWNmMDg5MWE2YzI0MzY0M2I4ZTU0NzZhZTIifX19";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM0YjM2ZGU3ZDY3OWI4YmJjNzI1NDk5YWRhZWYyNGRjNTE4ZjVhZTIzZTcxNjk4MWUxZGNjNmIyNzIwYWIifX19";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiNmViMjVkMWZhYWJlMzBjZjQ0NGRjNjMzYjU4MzI0NzVlMzgwOTZiN2UyNDAyYTNlYzQ3NmRkN2I5In19fQ==";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkxOTQ5NzNhM2YxN2JkYTk5NzhlZDYyNzMzODM5OTcyMjI3NzRiNDU0Mzg2YzgzMTljMDRmMWY0Zjc0YzJiNSJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3Y2FmNzU5MWIzOGUxMjVhODAxN2Q1OGNmYzY0MzNiZmFmODRjZDQ5OWQ3OTRmNDFkMTBiZmYyZTViODQwIn19fQ==";
        }
        throw new AssertionError("Invalid: " + digit);
    }
}