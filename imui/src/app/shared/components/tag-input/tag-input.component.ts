import { Component, ElementRef, HostListener, inject, input, model, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TagService } from '../../../services/tag.service';

@Component({
  selector: 'app-tag-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tag-input.component.html'
})
export class TagInputComponent implements OnInit {
  private readonly tagService = inject(TagService);
  private readonly elementRef = inject(ElementRef);

  readonly tagsString = model<string | undefined>('');
  readonly placeholder = input<string>('Select or type tags...');
  readonly domain = input<string>('INVESTMENT');

  readonly isOpen = signal<boolean>(false);
  readonly query = signal<string>('');

  get availableTags() {
    return this.tagService.tags();
  }

  readonly selectedTags = computed(() => {
    const raw = this.tagsString() || '';
    return raw
      .split(',')
      .map(t => t.trim())
      .filter(t => t.length > 0);
  });

  readonly filteredSuggestions = computed(() => {
    const q = this.query().trim().toLowerCase();
    const selected = new Set(this.selectedTags().map(t => t.toLowerCase()));

    return this.availableTags.filter(t => {
      const nameLower = t.name.toLowerCase();
      if (selected.has(nameLower)) return false;
      return !q || nameLower.includes(q);
    });
  });

  readonly canCreateNew = computed(() => {
    const q = this.query().trim();
    if (!q) return false;
    const qLower = q.toLowerCase();
    const selected = this.selectedTags().map(t => t.toLowerCase());
    if (selected.includes(qLower)) return false;
    return !this.availableTags.some(t => t.name.toLowerCase() === qLower);
  });

  ngOnInit(): void {
    this.tagService.loadTags(this.domain()).subscribe();
  }

  addTag(tagName: string): void {
    const trimmed = tagName.trim();
    if (!trimmed) return;

    const current = this.selectedTags();
    if (!current.some(t => t.toLowerCase() === trimmed.toLowerCase())) {
      const next = [...current, trimmed];
      this.tagsString.set(next.join(', '));

      // Persist tag to catalog if not existing
      if (!this.availableTags.some(t => t.name.toLowerCase() === trimmed.toLowerCase())) {
        this.tagService.createTag({ name: trimmed, domain: this.domain() }).subscribe();
      }
    }

    this.query.set('');
  }

  removeTag(tagName: string): void {
    const current = this.selectedTags();
    const next = current.filter(t => t.toLowerCase() !== tagName.toLowerCase());
    this.tagsString.set(next.join(', '));
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      const q = this.query().trim();
      if (q) {
        this.addTag(q);
      }
    } else if (event.key === 'Backspace' && !this.query() && this.selectedTags().length > 0) {
      const tags = this.selectedTags();
      this.removeTag(tags[tags.length - 1]);
    } else if (event.key === 'Escape') {
      this.isOpen.set(false);
    }
  }

  onInputFocus(): void {
    this.isOpen.set(true);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }
}
