package moze_intel.projecte.common.tag;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Since 26.2 the vanilla {@link TagAppender} only accepts {@link ResourceKey resource keys}, this wraps one so that our providers can keep adding the
 * registry objects themselves, which then get resolved to their keys.
 */
public class ElementTagAppender<T> implements TagAppender<T> {

	private final TagAppender<T> delegate;
	private final Function<T, ResourceKey<T>> keyGetter;

	public ElementTagAppender(TagAppender<T> delegate, Function<T, ResourceKey<T>> keyGetter) {
		this.delegate = delegate;
		this.keyGetter = keyGetter;
	}

	public ElementTagAppender<T> add(T element) {
		delegate.add(keyGetter.apply(element));
		return this;
	}

	@SafeVarargs
	public final ElementTagAppender<T> add(T... elements) {
		for (T element : elements) {
			add(element);
		}
		return this;
	}

	@Override
	public ElementTagAppender<T> add(ResourceKey<T> element) {
		delegate.add(element);
		return this;
	}

	@SafeVarargs
	@Override
	public final ElementTagAppender<T> add(ResourceKey<T>... elements) {
		for (ResourceKey<T> element : elements) {
			add(element);
		}
		return this;
	}

	@Override
	public ElementTagAppender<T> addAll(Collection<ResourceKey<T>> elements) {
		elements.forEach(this::add);
		return this;
	}

	@Override
	public ElementTagAppender<T> addAll(Stream<ResourceKey<T>> elements) {
		elements.forEach(this::add);
		return this;
	}

	@Override
	public ElementTagAppender<T> addOptional(ResourceKey<T> element) {
		delegate.addOptional(element);
		return this;
	}

	@Override
	public ElementTagAppender<T> addTag(TagKey<T> tag) {
		delegate.addTag(tag);
		return this;
	}

	@Override
	public ElementTagAppender<T> addOptionalTag(TagKey<T> tag) {
		delegate.addOptionalTag(tag);
		return this;
	}

	@SafeVarargs
	@Override
	public final ElementTagAppender<T> addTags(TagKey<T>... tags) {
		for (TagKey<T> tag : tags) {
			addTag(tag);
		}
		return this;
	}

	@Override
	public ElementTagAppender<T> add(TagEntry entry) {
		delegate.add(entry);
		return this;
	}

	@Override
	public ElementTagAppender<T> replace(boolean value) {
		delegate.replace(value);
		return this;
	}

	@Override
	public ElementTagAppender<T> remove(ResourceKey<T> element) {
		delegate.remove(element);
		return this;
	}

	@Override
	public ElementTagAppender<T> remove(TagKey<T> tag) {
		delegate.remove(tag);
		return this;
	}
}
