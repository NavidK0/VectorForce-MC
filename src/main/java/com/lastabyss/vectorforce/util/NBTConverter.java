/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lastabyss.vectorforce.util;

import com.sk89q.jnbt.*;
import net.minecraft.server.v1_9_R1.NBTBase;
import net.minecraft.server.v1_9_R1.NBTTagByte;
import net.minecraft.server.v1_9_R1.NBTTagByteArray;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagDouble;
import net.minecraft.server.v1_9_R1.NBTTagEnd;
import net.minecraft.server.v1_9_R1.NBTTagFloat;
import net.minecraft.server.v1_9_R1.NBTTagInt;
import net.minecraft.server.v1_9_R1.NBTTagIntArray;
import net.minecraft.server.v1_9_R1.NBTTagList;
import net.minecraft.server.v1_9_R1.NBTTagLong;
import net.minecraft.server.v1_9_R1.NBTTagShort;
import net.minecraft.server.v1_9_R1.NBTTagString;

import java.util.*;
import java.util.Map.Entry;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
public final class NBTConverter {

    public static NBTBase toNative(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return toNative((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return toNative((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return toNative((LongTag) tag);

        } else if (tag instanceof StringTag) {
            return toNative((StringTag) tag);

        } else if (tag instanceof IntTag) {
            return toNative((IntTag) tag);

        } else if (tag instanceof ByteTag) {
            return toNative((ByteTag) tag);

        } else if (tag instanceof ByteArrayTag) {
            return toNative((ByteArrayTag) tag);

        } else if (tag instanceof CompoundTag) {
            return toNative((CompoundTag) tag);

        } else if (tag instanceof FloatTag) {
            return toNative((FloatTag) tag);

        } else if (tag instanceof ShortTag) {
            return toNative((ShortTag) tag);

        } else if (tag instanceof DoubleTag) {
            return toNative((DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static NBTTagIntArray toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new NBTTagIntArray(Arrays.copyOf(value, value.length));
    }

    public static NBTTagList toNative(ListTag tag) {
        NBTTagList list = new NBTTagList();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static NBTTagLong toNative(LongTag tag) {
        return new NBTTagLong(tag.getValue());
    }

    public static NBTTagString toNative(StringTag tag) {
        return new NBTTagString(tag.getValue());
    }

    public static NBTTagInt toNative(IntTag tag) {
        return new NBTTagInt(tag.getValue());
    }

    public static NBTTagByte toNative(ByteTag tag) {
        return new NBTTagByte(tag.getValue());
    }

    public static NBTTagByteArray toNative(ByteArrayTag tag) {
        byte[] value = tag.getValue();
        return new NBTTagByteArray(Arrays.copyOf(value, value.length));
    }

    public static NBTTagCompound toNative(CompoundTag tag) {
        NBTTagCompound compound = new NBTTagCompound();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.set(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static NBTTagFloat toNative(FloatTag tag) {
        return new NBTTagFloat(tag.getValue());
    }

    public static NBTTagShort toNative(ShortTag tag) {
        return new NBTTagShort(tag.getValue());
    }

    public static NBTTagDouble toNative(DoubleTag tag) {
        return new NBTTagDouble(tag.getValue());
    }

    public static Tag fromNative(NBTBase other) {
        if (other instanceof NBTTagIntArray) {
            return fromNative((NBTTagIntArray) other);

        } else if (other instanceof NBTTagList) {
            return fromNative((NBTTagList) other);

        } else if (other instanceof NBTTagEnd) {
            return fromNative((NBTTagEnd) other);

        } else if (other instanceof NBTTagLong) {
            return fromNative((NBTTagLong) other);

        } else if (other instanceof NBTTagString) {
            return fromNative((NBTTagString) other);

        } else if (other instanceof NBTTagInt) {
            return fromNative((NBTTagInt) other);

        } else if (other instanceof NBTTagByte) {
            return fromNative((NBTTagByte) other);

        } else if (other instanceof NBTTagByteArray) {
            return fromNative((NBTTagByteArray) other);

        } else if (other instanceof NBTTagCompound) {
            return fromNative((NBTTagCompound) other);

        } else if (other instanceof NBTTagFloat) {
            return fromNative((NBTTagFloat) other);

        } else if (other instanceof NBTTagShort) {
            return fromNative((NBTTagShort) other);

        } else if (other instanceof NBTTagDouble) {
            return fromNative((NBTTagDouble) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayTag fromNative(NBTTagIntArray other) {
        int[] value = other.c();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(NBTTagList other) {
        other = (NBTTagList) other.clone();
        List<Tag> list = new ArrayList<Tag>();
        Class<? extends Tag> listClass = StringTag.class;
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            Tag child = fromNative(other.h(0));
            list.add(child);
            listClass = child.getClass();
        }
        return new ListTag(listClass, list);
    }

    public static EndTag fromNative(NBTTagEnd other) {
        return new EndTag();
    }

    public static LongTag fromNative(NBTTagLong other) {
        return new LongTag(other.c());
    }

    public static StringTag fromNative(NBTTagString other) {
        return new StringTag(other.a_());
    }

    public static IntTag fromNative(NBTTagInt other) {
        return new IntTag(other.d());
    }

    public static ByteTag fromNative(NBTTagByte other) {
        return new ByteTag(other.f());
    }

    public static ByteArrayTag fromNative(NBTTagByteArray other) {
        byte[] value = other.c();
        return new ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static CompoundTag fromNative(NBTTagCompound other) {
        @SuppressWarnings("unchecked") Collection<String> tags = other.c();
        Map<String, Tag> map = new HashMap<String, Tag>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(NBTTagFloat other) {
        return new FloatTag(other.h());
    }

    public static ShortTag fromNative(NBTTagShort other) {
        return new ShortTag(other.e());
    }

    public static DoubleTag fromNative(NBTTagDouble other) {
        return new DoubleTag(other.g());
    }

}